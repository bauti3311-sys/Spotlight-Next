package dev.bautista.spotlight.advanced;
interface IAdvancedService {
    int effectiveUid() = 1;
    String readSetting(String key) = 2;
    boolean canWriteSettings() = 3;
    void writeSetting(String key, String value, boolean confirmed) = 4;
    void stopApplication(String packageName, boolean confirmed) = 5;
    void destroy() = 16777114;
}
