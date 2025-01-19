package com.dsoftn.utils;

public class UNumbers {
    public static boolean isStringNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isStringInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Integer toInteger(Object object) {
        if (object == null) {
            return null;
        }

        try {
            if (object instanceof Integer) {
                return (Integer) object;
            }
            else if (object instanceof Double) {
                return ((Double) object).intValue();
            }
            else if (object instanceof String) {
                Double doubleValue = toDouble(object.toString());
                if (doubleValue != null) {
                    return doubleValue.intValue();
                }
                }
            else if (object instanceof Long) {
                return ((Long) object).intValue();
            }
            else if (object instanceof Float) {
                return ((Float) object).intValue();
            }
            Double doubleValue = toDouble(object.toString());
            if (doubleValue != null) {
                return doubleValue.intValue();
            }
            return Integer.parseInt(object.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double toDouble(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            if (object instanceof Double) {
                return (Double) object;
            }
            else if (object instanceof Integer) {
                return ((Integer) object).doubleValue();
            }
            else if (object instanceof String) {
                return Double.parseDouble((String) object);
            }
            else if (object instanceof Long) {
                return ((Long) object).doubleValue();
            }
            else if (object instanceof Float) {
                return ((Float) object).doubleValue();
            }
            return Double.parseDouble(object.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Float toFloat(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            if (object instanceof Float) {
                return (Float) object;
            }
            else if (object instanceof Integer) {
                return ((Integer) object).floatValue();
            }
            else if (object instanceof Double) {
                return ((Double) object).floatValue();
            }
            else if (object instanceof String) {
                return Float.parseFloat((String) object);
            }
            else if (object instanceof Long) {
                return ((Long) object).floatValue();
            }
            return Float.parseFloat(object.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long toLong(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            if (object instanceof Long) {
                return (Long) object;
            }
            else if (object instanceof Integer) {
                return ((Integer) object).longValue();
            }
            else if (object instanceof Double) {
                return ((Double) object).longValue();
            }
            else if (object instanceof String) {
                return Long.parseLong((String) object);
            }
            else if (object instanceof Float) {
                return ((Float) object).longValue();
            }
            return Long.parseLong(object.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer getPercentIfHasNoRemainder(int totalRows, int currentRow) {
        if (totalRows == 0) {
            return null;
        }

        Double percent = ((double) currentRow / (double) totalRows) * 100.0;

        // Return percent if it has no remainder else return null
        if (percent % 1 == 0) {
            return percent.intValue();
        }
        else {
            return null;
        }
    }

}
