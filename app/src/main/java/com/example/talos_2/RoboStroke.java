/*
 * Copyright (c) 2011 Tal Shalif
 * 
 * This file is part of Talos-Rowing.
 * 
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.example.talos_2;

import com.example.talos_2.acceleration.AccelerationFilter;
import com.example.talos_2.acceleration.GravityFilter;
import com.example.talos_2.common.SimpleLock;
import com.example.talos_2.data.AxisDataReverseFilter;
import com.example.talos_2.data.AxisDataSwapFilter;
import com.example.talos_2.data.DataIdx;
import com.example.talos_2.data.DataRecord.Type;
import com.example.talos_2.data.ErrorListener;
import com.example.talos_2.data.FileDataInput;
import com.example.talos_2.data.SensorDataFilter;
import com.example.talos_2.data.SensorDataInput;
import com.example.talos_2.data.SensorDataSink;
import com.example.talos_2.data.SensorDataSource;
import com.example.talos_2.data.SessionRecorder;
import com.example.talos_2.data.remote.DataRemote.DataRemoteError;
import com.example.talos_2.data.remote.DataSender;
import com.example.talos_2.data.remote.SessionBroadcaster;
import com.example.talos_2.param.ParameterBusEventData;
import com.example.talos_2.param.ParameterService;
import com.example.talos_2.stroke.RollScanner;
import com.example.talos_2.stroke.RowingDetector;
import com.example.talos_2.stroke.StrokePowerScanner;
import com.example.talos_2.stroke.StrokeRateScanner;
import com.example.talos_2.way.DistanceResolver;
import com.example.talos_2.way.DistanceResolverDefault;
import com.example.talos_2.way.GPSDataFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * A RoboStroke engine initializer.
 * This is a handy class for initializing and connecting the various data input
 * filters and processors. A user of this class will usually set event listeners
 * by calling @link StrokeRateScanner#setStrokeListener(com.example.talos_2.stroke.StrokeRateListener) strokeRateScanner.setStrokeRateListener}
 * and @link GPSDataFilter#setWayListener(com.example.talos_2.way.WayListener) gpsFilter.setWayListener}. The client can also register for
 * raw Sensor data events by attaching a {@link SensorDataSink} to any {@link SensorDataSource} or {@link SensorDataFilter} object. 
 *
 */
public class RoboStroke {
	
	private static final Logger logger = LoggerFactory.getLogger(com.example.talos_2.RoboStroke.class);
	
	/**
	 * wraps the real SensorDataInput and does event recording
	 */
	private SensorDataInput dataInput;
	
	private final SimpleLock inputLock = new SimpleLock();
	/**
	 * filters-out gravity from acceleration data 
	 */
	private GravityFilter gravityFilter;
	
	/**
	 * scans acceleration event to detect stroke-rate
	 */
	private StrokeRateScanner strokeRateScanner;
	
	/**
	 * detects and notify ROWING_START and ROWING_STOP
	 */
	
	private RowingDetector rowingDetector;
	
	/**
	 * scans acceleration event to detect stroke-power
	 */
	private StrokePowerScanner strokePowerScanner;	

	/**
	 * scans orientation and stroke events to detect boat-roll
	 */
	private RollScanner rollScanner;	

	/**
	 * combines gravity-filtered acceleration forces to uni-directional acceleration/deceleration data
	 */
	private SensorDataFilter accelerationFilter;

	/**
	 * processes GPS/Location sensor data for determining stroking distance and speed 
	 */
	private GPSDataFilter gpsFilter;

	/**
	 * error listener
	 */
	private ErrorListener errorListener;

	/**
	 * Singleton event bus instance
	 */
	private final com.example.talos_2.RoboStrokeEventBus bus = new com.example.talos_2.RoboStrokeEventBus();
	
	private final ParameterService parameters = new ParameterService(bus);
	

	/**
	 * data/event logger when recording is on
	 */
	private final SessionRecorder recorder = new SessionRecorder(this);

	private SessionBroadcaster sessionBroadcaster;

	private boolean broadcastOn;
	
	private AxisDataReverseFilter coaxModeOrientationFilter;
	
	private AxisDataSwapFilter landscapeAccelFilter;

	private AxisDataSwapFilter landscapeOrientationFilter;

	private final com.example.talos_2.BusEventListener sessionParamChangeListener;
	
	private final com.example.talos_2.ParamKeys[] sessionParamList = {
			com.example.talos_2.ParamKeys.PARAM_SENSOR_ORIENTATION_REVERSED
	};

	/**
	 * constructor with the <code>DistanceResolverDefault</code>
	 */
	public RoboStroke() {
		this(new DistanceResolverDefault());
	}
	
	public RoboStroke(DistanceResolver distanceResolver) {
		this(distanceResolver, null);
	}
	
	/**
	 * constructor with the <code>DistanceResolver</code> implementation.
	 * @param distanceResolver a client provided implementation that can extract distance from location events 
	 */
	public RoboStroke(DistanceResolver distanceResolver, DataSender dataSenderImpl) {
				
		com.example.talos_2.ParamRegistration.installParams(parameters);

		try {
			sessionBroadcaster = new SessionBroadcaster(this, dataSenderImpl);
		} catch (DataRemoteError e) {
			logger.error("failed to create sessionBroadcaster", e);
		}
		
		initPipeline(distanceResolver);
		
		parameters.addListener(com.example.talos_2.ParamKeys.PARAM_SESSION_BROADCAST_ON.getId(), param -> {
			synchronized (inputLock) {

				broadcastOn = param.getValue();

				if (dataInput != null) {
					if (sessionBroadcaster != null) sessionBroadcaster.enable(broadcastOn);
				}

			}
		});
		
		parameters.addListener(com.example.talos_2.ParamKeys.PARAM_SESSION_BROADCAST_PORT.getId(), param -> {
			if (sessionBroadcaster != null)  sessionBroadcaster.setPort(param.getValue());

		});
		
		parameters.addListener(com.example.talos_2.ParamKeys.PARAM_SESSION_BROADCAST_HOST.getId(), param -> {
			if (sessionBroadcaster != null) sessionBroadcaster.setAddress(param.getValue());

		});

		parameters.addListener(com.example.talos_2.ParamKeys.PARAM_SENSOR_ORIENTATION_REVERSED.getId(), param -> setCoaxMode(param.getValue()));
		
		parameters.addListener(com.example.talos_2.ParamKeys.PARAM_SENSOR_ORIENTATION_LANDSCAPE.getId(), param -> setLandscapeMode(param.getValue()));
		
		setCoaxMode((Boolean)parameters.getValue(com.example.talos_2.ParamKeys.PARAM_SENSOR_ORIENTATION_REVERSED.getId()));
		
		sessionParamChangeListener = event -> {
			if (event.type == Type.SESSION_PARAMETER) {
				ParameterBusEventData pd = (ParameterBusEventData) event.data;

				if (pd.id.equals(ParamKeys.PARAM_SENSOR_ORIENTATION_REVERSED.getId())) {
					parameters.setParam(pd.id, pd.value);
				}
			}
		};
	}

	private void setLandscapeMode(boolean value) {
		landscapeOrientationFilter.setEnabled(value);
		landscapeAccelFilter.setEnabled(value);
	}

	private void setCoaxMode(boolean value) {
		coaxModeOrientationFilter.setEnabled(value);
	}

	/**
	 * get shared event bus instance
	 * @return global event bus
	 */
	public com.example.talos_2.RoboStrokeEventBus getBus() {
		return bus;
	}

	/**
	 * sets the error listener of the event pipeline
	 * @param errorListener
	 */
	public void setErrorListener(ErrorListener errorListener) {
		this.errorListener = errorListener;
	}

	/**
	 * initialize and connect the sensor data pipelines
	 * @param distanceResolver
	 */
	private void initPipeline(DistanceResolver distanceResolver) {

		landscapeAccelFilter = new AxisDataSwapFilter(DataIdx.ACCEL_Y, DataIdx.ACCEL_X);
		landscapeOrientationFilter = new AxisDataSwapFilter(DataIdx.ORIENT_PITCH, DataIdx.ORIENT_ROLL);

		coaxModeOrientationFilter = new AxisDataReverseFilter(DataIdx.ORIENT_PITCH, DataIdx.ORIENT_ROLL);

		accelerationFilter = new AccelerationFilter(this);
		gravityFilter = new GravityFilter(this, accelerationFilter);
		
		strokeRateScanner = new StrokeRateScanner(this);
		rowingDetector = new RowingDetector(this); 
		strokePowerScanner = new StrokePowerScanner(this, strokeRateScanner);
		accelerationFilter.addSensorDataSink(strokeRateScanner);
		accelerationFilter.addSensorDataSink(strokePowerScanner);
		accelerationFilter.addSensorDataSink(rowingDetector);
		gpsFilter = new GPSDataFilter(this, distanceResolver);
		rollScanner = new RollScanner(bus);
	}
	

	/**
	 * Set the sensor data input to replay from a file.
	 * look at the code in @link LoggingSensorDataInput#logData} to see what
	 * the data file content should look like.
	 * @param file replay input file
	 * @throws IOException
	 */
	public void setFileInput(File file) throws IOException {
		setInput(new FileDataInput(this, file));
	}
	

	/**
	 * Set the sensor data input to a real device dependant implementation
	 * @param dataInput device input implementation
	 */
	public void setInput(SensorDataInput dataInput) {
		synchronized (inputLock) {
			stop();

			if (dataInput != null) {

				bus.fireEvent(Type.INPUT_START, null);

				if (!dataInput.isLocalSensorInput()) {

					for (com.example.talos_2.ParamKeys k: sessionParamList) {
						parameters.getParam(k.getId()).saveValue();
					}

					bus.addBusListener(sessionParamChangeListener);
				}


				this.dataInput = dataInput;
				
				connectPipeline();

				sessionBroadcaster.enable(broadcastOn);
				
				dataInput.start();
			}
		}
	}
	
	public boolean isSeekableDataInput() {
		SensorDataInput di = dataInput;
		return di != null && di.isSeekable();
	}

	/**
	 * Stop processing
	 */
	public void stop() {
		
		synchronized (inputLock) {
			if (dataInput != null) {

				sessionBroadcaster.enable(false);

				dataInput.setErrorListener(null);
				dataInput.stop();

				coaxModeOrientationFilter.clearSensorDataSinks();
				landscapeAccelFilter.clearSensorDataSinks();
				landscapeOrientationFilter.clearSensorDataSinks();
				gpsFilter.reset();

				if (!dataInput.isLocalSensorInput()) {

					for (com.example.talos_2.ParamKeys k : sessionParamList) {
						parameters.getParam(k.getId()).restoreValue();
					}

					bus.removeBusListener(sessionParamChangeListener);
				}

				bus.fireEvent(Type.INPUT_STOP, null);
			}
		}
		
	}
	
	/**
	 * get <code>SensorDataInput</code> implemention currently in use
	 * @return SensorDataInput implemention
	 */
	public SensorDataInput getDataInput() {
		return dataInput;
	}

	/**
	 * Get the gravity filter object.
	 * GravityFilter normalizes-out gravity from row acceleration data 
	 * @return
	 */
	public GravityFilter getGravityFilter() {
		return gravityFilter;
	}

	/**
	 * Get the stroke rate scanner object.
	 * <code>StrokeRateScanner</code> scans acceleration event to detect the stroke-rate
	 * @return StrokeRateScanner object
	 */
	public StrokeRateScanner getStrokeRateScanner() {
		return strokeRateScanner;
	}

	/**
	 * Get the stroke power scanner object.
	 * <code>StrokePowerScanner</code> scans acceleration event to detect the stroke-power
	 * @return StrokePowerScanner object
	 */
	public StrokePowerScanner getStrokePowerScanner() {
		return strokePowerScanner;
	}

	/**
	 * Get the acceleration combiner object.
	 * <code>AccelerationFilter</code> combines the gravity-filtered acceleration forces to uni-directional acceleration/deceleration data
	 * @return AccelerationFilter object
	 */
	public SensorDataSource getAccelerationSource() {
		return accelerationFilter;
	}

	/**
	 * Get GPS data processor.
	 * @return AccelerationFilter object
	 */
	public GPSDataFilter getGpsFilter() {
		return gpsFilter;
	}
	
	
	/**
	 * get roll scanner
	 * @return roll scanner
	 */
	public SensorDataSource getOrientationSource() {
		return rollScanner;
	}

	/**
	 * connects a new DataInputSource to the sensor data pipelines
	 */
	private void connectPipeline() {
		
		dataInput.setErrorListener(errorListener);		
		
		if (dataInput.isLocalSensorInput()) {
			dataInput.getOrientationDataSource().addSensorDataSink(landscapeOrientationFilter, 0.0);
			dataInput.getAccelerometerDataSource().addSensorDataSink(landscapeAccelFilter, 0.0);	
		}
		
		dataInput.getOrientationDataSource().addSensorDataSink(gravityFilter.getOrientationDataSink());
		
		dataInput.getOrientationDataSource().addSensorDataSink(coaxModeOrientationFilter);

		dataInput.getOrientationDataSource().addSensorDataSink(rollScanner);
		
		dataInput.getAccelerometerDataSource().addSensorDataSink(gravityFilter);
		
		dataInput.getGPSDataSource().addSensorDataSink(gpsFilter);
	}

	public void setDataLogger(File logFile) throws IOException {
		recorder.setDataLogger(logFile);	
	}

	public ParameterService getParameters() {
		return parameters;
	}	
	
	@Override
	protected void finalize() throws Throwable {
		
		destroy();
		
		super.finalize();
	}

	public void destroy() {
		bus.shutdown();
		try {
			setDataLogger(null);
		} catch (IOException e) {
			logger.error("exception thrown when closing session log file", e);
		}
	}
}
