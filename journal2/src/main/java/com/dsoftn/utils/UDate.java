package com.dsoftn.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;


public class UDate {
    public static String getDateShort(LocalDate dateOBJ) {
        return dateOBJ.format(CONSTANTS.DATE_FORMATTER);
    }

    public static String getDateWithMonthName(LocalDate dateOBJ) {
        String result = "";

        result += dateOBJ.getDayOfMonth() + ". ";
        result += OBJECTS.SETTINGS.getl("month_" + dateOBJ.getMonthValue()) + " ";
        result += dateOBJ.getYear() + ".";

        return result;
    }

    public static String getDateWithWeekday(LocalDate dateOBJ) {
        String result = "";

        result += OBJECTS.SETTINGS.getl("weekday_" + dateOBJ.getDayOfWeek().getValue()) + " ";
        result += getDateShort(dateOBJ);

        return result;
    }
    
    public static String getDateWithWeekdayAndMonthName(LocalDate dateOBJ) {
        String result = "";

        result += OBJECTS.SETTINGS.getl("weekday_" + dateOBJ.getDayOfWeek().getValue()) + " ";
        result += getDateWithMonthName(dateOBJ);

        return result;
    }

    /**
     * Returns period string like "Before 1 month and 2 days"
     */
    public static String getPeriodString(LocalDate dateFrom, LocalDate dateTo) {
        Period period = Period.between(dateFrom, dateTo);

        // Check if period is Today
        if (period.getYears() == 0 && period.getMonths() == 0 && period.getDays() == 0) {
            return OBJECTS.SETTINGS.getl("text_Today") + ".";
        }
        // Check if period is Yesterday
        else if (period.getYears() == 0 && period.getMonths() == 0 && period.getDays() == -1) {
            return OBJECTS.SETTINGS.getl("text_Yesterday") + ".";
        }
        // Check if period is Day before Yesterday
        else if (period.getYears() == 0 && period.getMonths() == 0 && period.getDays() == -2) {
            return OBJECTS.SETTINGS.getl("text_DayBeforeYesterday") + ".";
        }
        // Check if period is Tomorrow
        else if (period.getYears() == 0 && period.getMonths() == 0 && period.getDays() == 1) {
            return OBJECTS.SETTINGS.getl("text_Tomorrow") + ".";
        }
        // Check if period is Day after Tomorrow
        else if (period.getYears() == 0 && period.getMonths() == 0 && period.getDays() == 2) {
            return OBJECTS.SETTINGS.getl("text_DayAfterTomorrow") + ".";
        }

        List<String> periodList = new ArrayList<>();
        
        // Check if period is in the past or future
        if (period.getYears() < 0 || period.getMonths() < 0 || period.getDays() < 0) {
            periodList.add(OBJECTS.SETTINGS.getl("period_Before"));
        } else {
            periodList.add(OBJECTS.SETTINGS.getl("period_After"));
        }

        // Calculate years
        if (period.getYears() == -1) {
            periodList.add(OBJECTS.SETTINGS.getl("period_Before1Year"));
        } else if (period.getYears() < -1 && period.getYears() > -5) {
            periodList.add(OBJECTS.SETTINGS.getl("period_Before2_4Year").replace("#1", String.valueOf(Math.abs(period.getYears()))));
        } else if (period.getYears() <= -5) {
            periodList.add(OBJECTS.SETTINGS.getl("period_Before5Years").replace("#1", String.valueOf(Math.abs(period.getYears()))));
        } else if (period.getYears() == 1) {
            periodList.add(OBJECTS.SETTINGS.getl("period_After1Year"));
        } else if (period.getYears() > 1 && period.getYears() < 5) {
            periodList.add(OBJECTS.SETTINGS.getl("period_After2_4Year").replace("#1", String.valueOf(period.getYears())));
        } else if (period.getYears() >= 5) {
            periodList.add(OBJECTS.SETTINGS.getl("period_After5Years").replace("#1", String.valueOf(period.getYears())));
        }

        // Calculate months
        if (period.getMonths() == -1) {
            periodList.add(OBJECTS.SETTINGS.getl("period_Before1Month"));
        } else if (period.getMonths() < -1 && period.getMonths() > -5) {
            periodList.add(OBJECTS.SETTINGS.getl("period_Before2_4Month").replace("#1", String.valueOf(Math.abs(period.getMonths()))));
        } else if (period.getMonths() <= -5) {
            periodList.add(OBJECTS.SETTINGS.getl("period_Before5Months").replace("#1", String.valueOf(Math.abs(period.getMonths()))));
        } else if (period.getMonths() == 1) {
            periodList.add(OBJECTS.SETTINGS.getl("period_After1Month"));
        } else if (period.getMonths() > 1 && period.getMonths() < 5) {
            periodList.add(OBJECTS.SETTINGS.getl("period_After2_4Month").replace("#1", String.valueOf(period.getMonths())));
        } else if (period.getMonths() >= 5) {
            periodList.add(OBJECTS.SETTINGS.getl("period_After5Months").replace("#1", String.valueOf(period.getMonths())));
        }

        // Calculate days
        if (period.getDays() == -1) {
            periodList.add(OBJECTS.SETTINGS.getl("period_Before1Day"));
        } else if (period.getDays() < -1) {
            periodList.add(OBJECTS.SETTINGS.getl("period_BeforeMoreDays").replace("#1", String.valueOf(Math.abs(period.getDays()))));
        } else if (period.getDays() == 1) {
            periodList.add(OBJECTS.SETTINGS.getl("period_After1Day"));
        } else if (period.getDays() > 1) {
            periodList.add(OBJECTS.SETTINGS.getl("period_AfterMoreDays").replace("#1", String.valueOf(period.getDays())));
        }

        String result = "";

        for (int i = 0; i < periodList.size(); i++) {
            if (i == 0) {
                result += periodList.get(i);
                continue;
            }

            if (i == periodList.size() - 1 && periodList.size() > 2) {
                result += " " + OBJECTS.SETTINGS.getl("text_and") + " " + periodList.get(i) + ".";
                continue;
            }

            result += " " + periodList.get(i);
        }

        if (periodList.size() == 2) {
            result += ".";
        }

        return result;
    }

    /**
     * Checks if string is valid date, if date does not end with "." it will be still considered valid
     */
    public static boolean isStringValidDate(String date) {
        try {
            if (!date.endsWith(".")) {
                date += ".";
            }
            LocalDate.parse(date, CONSTANTS.DATE_FORMATTER_UNIVERSAL);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if string is valid time. Allows time with or without seconds
     * @param time
     * @return
     */
    public static boolean isStringValidTime(String time) {
        try {
            if (UString.Count(time, ":") == 1) {
                time += ":00";
            }
            LocalTime.parse(time, CONSTANTS.TIME_FORMATTER_UNIVERSAL);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
