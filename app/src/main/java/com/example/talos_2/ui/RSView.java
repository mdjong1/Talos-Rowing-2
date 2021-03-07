package com.example.talos_2.ui;

public interface RSView extends HasVisibility, HasBackgroundColor {
	void setOnLongClickListener(RSLongClickListener listener);
	void setOnClickListener(com.example.talos_2.ui.RSClickListener listener);
	void setOnDoubleClickListener(com.example.talos_2.ui.RSDoubleClickListener listener);
}
