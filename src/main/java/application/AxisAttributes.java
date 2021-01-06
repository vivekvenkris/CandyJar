package application;

import java.util.function.Function;

import data_holders.Candidate;
import de.gsi.dataset.spi.utils.Tuple;

public class AxisAttributes {
	private String label;
	private String unit;
	private Boolean log;
	private Integer tickUnit;
	private Boolean autoRanging;
	private Double autoRangePadding;
	private Boolean showErrors;
	private Function<Candidate, Tuple<Double, Double>> valueFunction;
	
	

	public AxisAttributes(String label, String unit, Boolean log, Integer tickUnit, Boolean autoRanging,
			Double autoRangePadding, Boolean showErrors,Function<Candidate, Tuple<Double, Double>> valueFunction) {
		super();
		this.label = label;
		this.unit = unit;
		this.log = log;
		this.tickUnit = tickUnit;
		this.autoRanging = autoRanging;
		this.autoRangePadding = autoRangePadding;
		this.showErrors = showErrors;
		this.valueFunction = valueFunction;
	}
	
	
	public AxisAttributes(String label, String unit, Boolean log, Boolean showErrors, 
			Function<Candidate, Tuple<Double, Double>> valueFunction) {
		super();
		this.label = label;
		this.unit = unit;
		this.log = log;
		this.tickUnit = 1;
		this.autoRanging = false;
		this.autoRangePadding = 0.05;
		this.showErrors = showErrors;
		this.valueFunction = valueFunction;
	}
	
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public Boolean getLog() {
		return log;
	}
	public void setLog(Boolean log) {
		this.log = log;
	}
	public Integer getTickUnit() {
		return tickUnit;
	}
	public void setTickUnit(Integer tickUnit) {
		this.tickUnit = tickUnit;
	}
	public Boolean getAutoRanging() {
		return autoRanging;
	}
	public void setAutoRanging(Boolean autoRanging) {
		this.autoRanging = autoRanging;
	}
	public Double getAutoRangePadding() {
		return autoRangePadding;
	}
	public void setAutoRangePadding(Double autoRangePadding) {
		this.autoRangePadding = autoRangePadding;
	}
	public Boolean getShowErrors() {
		return showErrors;
	}
	public void setShowErrors(Boolean showErrors) {
		this.showErrors = showErrors;
	}
	public Function<Candidate, Tuple<Double, Double>> getValueFunction() {
		return valueFunction;
	}
	public void setValueFunction(Function<Candidate, Tuple<Double, Double>> valueFunction) {
		this.valueFunction = valueFunction;
	}

	
	

}
