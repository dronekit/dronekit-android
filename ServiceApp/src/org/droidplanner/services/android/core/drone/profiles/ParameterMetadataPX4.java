package org.droidplanner.services.android.core.drone.profiles;

/**
 * Created by KiBa on 2015/12/21.
 */
public class ParameterMetadataPX4 extends ParameterMetadata{

    private String decimal;
    private String groupName;
    private String defaultAttr;
    private String typeAttr;
    private String max;
    private String min;

    public String getDecimal() {
        return decimal;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getMax() {
        return max;
    }

    public String getMin() {
        return min;
    }

    public String getDefaultAttr() {
        return defaultAttr;
    }

    public String getTypeAttr() {
        return typeAttr;
    }

    public void setDecimal(String decimal) {
        this.decimal = decimal;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setDefaultAttr(String defaultAttr) {
        this.defaultAttr = defaultAttr;
    }

    public void setTypeAttr(String typeAttr) {
        this.typeAttr = typeAttr;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String toString(){
          String str =  " group = " + getGroupName() +
                        "\n name = " + getName() +
                        "\n default = " + getDefaultAttr() +
                        "\n type = " + getTypeAttr() +
                        "\n display = " + getDisplayName() +
                        "\n description = " + getDescription() +
                        "\n unit = " + getUnits() +
                        "\n range = " + getRange() +
                        "\n values = " + getValues();
        return str;
    }
}
