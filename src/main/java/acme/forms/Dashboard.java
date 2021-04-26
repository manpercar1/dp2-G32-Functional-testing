/*
 * Dashboard.java
 *
 * Copyright (C) 2012-2021 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.forms;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Dashboard implements Serializable {

	protected static final long	serialVersionUID	= 1L;

	
	private Integer totalPublicTasks;
	private Integer totalPrivateTasks;
	private Integer totalFinishedTasks;
	private Integer totalNonFinishedTasks;
	private Double averageTaskExecutionPeriod;
	private Double deviationTaskExecutionPeriod;
	private Double minimunTaskExecutionPeriod;
	private Double maximunTaskExecutionPeriod;
	private BigDecimal averageTaskWorkloads;
	private BigDecimal deviationTaskWorkload;
	private Double minimunTaskWorkload;
	private Double maximumTaskWorkload;
}
