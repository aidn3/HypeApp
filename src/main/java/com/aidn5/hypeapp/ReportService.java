package com.aidn5.hypeapp;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

class ReportService implements ReportSenderFactory {

	@Override
	public boolean enabled(@NonNull CoreConfiguration config) {
		return true;
	}

	@NonNull
	@Override
	public ReportSender create(@NonNull Context context, @NonNull CoreConfiguration config) {
		return new ReportSender_();
	}

	class ReportSender_ implements ReportSender {

		@Override
		public void send(@NonNull Context context, @NonNull CrashReportData errorContent) {
			// TODO: 3/1/2019 [feature] ReportService
		}
	}
}
