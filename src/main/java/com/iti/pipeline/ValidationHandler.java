package com.iti.pipeline;

import com.iti.models.MediationContext;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

public class ValidationHandler extends MediationHandler {

    private static final Logger LOGGER = Logger.getLogger(ValidationHandler.class.getName());
    // مثال: نعتبر سجلات أقدم من سنة غير مقبولة
    private static final long MAX_AGE_SECONDS = 60L * 60 * 24 * 365;

    @Override
    protected MediationStepResult handleContext(MediationContext context) {
        // 1. التحقق من الحقول الأساسية
        String msisdn = context.getAsString("msisdn");
        String target = context.getAsString("target");
        String serviceType = context.getAsString("serviceType");
        Double usage = context.getAsDouble("usage");
        String unit = context.getAsString("unit");
        String startTimeStr = context.getAsString("start_time");
        String endTimeStr = context.getAsString("end_time");

        if (msisdn == null || msisdn.isEmpty()) {
            return new MediationStepResult(context, false, "Missing msisdn");
        }
        if (target == null || target.isEmpty()) {
            return new MediationStepResult(context, false, "Missing target");
        }
        if (serviceType == null || serviceType.isEmpty()) {
            return new MediationStepResult(context, false, "Missing serviceType");
        }
        if (usage == null) {
            return new MediationStepResult(context, false, "Missing usage");
        }
        if (unit == null || unit.isEmpty()) {
            return new MediationStepResult(context, false, "Missing unit");
        }
        if (startTimeStr == null || startTimeStr.isEmpty()) {
            return new MediationStepResult(context, false, "Missing start_time");
        }
        if (endTimeStr == null || endTimeStr.isEmpty()) {
            return new MediationStepResult(context, false, "Missing end_time");
        }

        // 2. تحقق من قيمة الاستخدام
        if (usage <= 0) {
            return new MediationStepResult(context, false, "Zero or negative usage");
        }

        // 3. تحقق من نوع الخدمة: نسمح فقط VOICE, SMS, DATA
        String svcUpper = serviceType.toUpperCase();
        if (!("VOICE".equals(svcUpper) || "SMS".equals(svcUpper) || "DATA".equals(svcUpper))) {
            return new MediationStepResult(context, false, "Unsupported serviceType: " + serviceType);
        }

        // 4. تحقق من الصيغة الزمنية والمدة
        Instant now = Instant.now();
        Instant startInstant;
        Instant endInstant;
        try {
            startInstant = Instant.parse(startTimeStr);
            endInstant = Instant.parse(endTimeStr);
        } catch (DateTimeParseException e) {
            return new MediationStepResult(context, false, "Invalid timestamp format");
        }
        // تأكد start <= end
        if (startInstant.isAfter(endInstant)) {
            return new MediationStepResult(context, false, "start_time is after end_time");
        }
        // لا نقبل سجلات في المستقبل
        if (startInstant.isAfter(now) || endInstant.isAfter(now)) {
            return new MediationStepResult(context, false, "Timestamp in the future");
        }
        // لا نقبل سجلات أقدم من سنة
        Instant tooOld = now.minusSeconds(MAX_AGE_SECONDS);
        if (startInstant.isBefore(tooOld) || endInstant.isBefore(tooOld)) {
            return new MediationStepResult(context, false, "Timestamp too old");
        }

        // جميع الشروط اجتازت
        return new MediationStepResult(context, true, "Valid record");
    }
}
