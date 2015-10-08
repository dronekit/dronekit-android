package org.droidplanner.services.android.core.parameters;

import java.io.Serializable;
import java.text.DecimalFormat;

import com.MAVLink.common.msg_param_value;

public class Parameter implements Comparable<Parameter>, Serializable {

	public String name;
	public double value;
	public int type;

	private final static DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
	static {
		format.applyPattern("0.###");
	}

	public Parameter(String name, double value, int type) {
		this.name = name;
		this.value = value;
		this.type = type;
	}

	public Parameter(msg_param_value m_value) {
		this(m_value.getParam_Id(), m_value.param_value, m_value.param_type);
	}

	public Parameter(String name, Double value) {
		this(name, value, 0); // TODO Setting type to Zero may cause an error
	}

	public Parameter(String name) {
		this(name, 0, 0); // TODO Setting type to Zero may cause an error
	}

	public String getValue() {
		return format.format(value);
	}

	public static DecimalFormat getFormat() {
		return format;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parameter)) return false;

        Parameter parameter = (Parameter) o;

        return (name == null ? parameter.name == null : name.equals(parameter.name));
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public int compareTo(Parameter another) {
        return name.compareTo(another.name);
    }
}
