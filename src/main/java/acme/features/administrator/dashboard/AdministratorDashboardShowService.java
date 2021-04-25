
package acme.features.administrator.dashboard;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.tasks.Task;
import acme.forms.Dashboard;
import acme.framework.components.Model;
import acme.framework.components.Request;
import acme.framework.entities.Administrator;
import acme.framework.services.AbstractShowService;

@Service
public class AdministratorDashboardShowService implements AbstractShowService<Administrator, Dashboard> {

	@Autowired
	AdministratorDashboardRepository repository;

	private static final MathContext mc = new MathContext(2, RoundingMode.HALF_DOWN);
	//	@Autowired
	//	Dashboard dashboard;


	@Override
	public boolean authorise(final Request<Dashboard> request) {
		assert request != null;

		return true;
	}

	@Override
	public void unbind(final Request<Dashboard> request, final Dashboard entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		request.unbind(entity, model, "totalPublicTasks", "totalPrivateTasks", "totalFinishedTasks", "totalNonFinishedTasks", "averageTaskExecutionPeriod", "deviationTaskExecutionPeriod", "minimunTaskExecutionPeriod", "maximunTaskExecutionPeriod",
			"averageTaskWorkloads", "deviationTaskWorkload", "minimunTaskWorkload", "maximumTaskWorkload");
	}

	@Override
	public Dashboard findOne(final Request<Dashboard> request) {
		assert request != null;

		final Dashboard result;
		final Integer totalPublicTasks;
		final Integer totalPrivateTasks;
		final Integer totalFinishedTasks;
		final Integer totalNonFinishedTasks;
		final Double averageTaskExecutionPeriod;
		final Double deviationTaskExecutionPeriod;
		final Double minimunTaskExecutionPeriod;
		final Double maximunTaskExecutionPeriod;
		final BigDecimal averageTaskWorkloads;
		final BigDecimal deviationTaskWorkload;
		final Double minimunTaskWorkload;
		final Double maximumTaskWorkload;

		result = new Dashboard();
		totalPublicTasks = this.repository.totalPublicTasks();
		totalPrivateTasks = this.repository.totalPrivateTasks();
		totalFinishedTasks = this.repository.totalFinishedTasks(new Date());
		totalNonFinishedTasks = this.repository.totalNonFinishedTasks(new Date());
		final Collection<Long> periods = this.calculatePeriods();
		averageTaskExecutionPeriod = this.avgPeriodTask(result, periods);
		deviationTaskExecutionPeriod = this.deviationPeriodTask(result, periods);
		minimunTaskExecutionPeriod = this.minimunTaskExecutionPeriod(periods);
		maximunTaskExecutionPeriod = this.maximunTaskExecutionPeriod(periods);
		averageTaskWorkloads = this.avgWorkloadTask();
		result.setAverageTaskWorkloads(averageTaskWorkloads);
		deviationTaskWorkload = this.devTaskWorkload(result);
		minimunTaskWorkload = this.repository.minimunTaskWorkload();
		maximumTaskWorkload = this.repository.maximumTaskWorkload();

		result.setTotalPublicTasks(totalPublicTasks);
		result.setTotalPrivateTasks(totalPrivateTasks);
		result.setTotalFinishedTasks(totalFinishedTasks);
		result.setTotalNonFinishedTasks(totalNonFinishedTasks);
		result.setAverageTaskExecutionPeriod(averageTaskExecutionPeriod);
		result.setDeviationTaskExecutionPeriod(deviationTaskExecutionPeriod);
		result.setMinimunTaskExecutionPeriod(minimunTaskExecutionPeriod);
		result.setMaximunTaskExecutionPeriod(maximunTaskExecutionPeriod);
		result.setDeviationTaskWorkload(deviationTaskWorkload);
		result.setMinimunTaskWorkload(minimunTaskWorkload);
		result.setMaximumTaskWorkload(maximumTaskWorkload);

		return result;
	}

	/** Aux **/

	private Collection<Long> calculatePeriods() {
		final Collection<Task> allTasks = this.repository.allTasks();
		final List<Long> periodsList = new ArrayList<>();

		for (final Task t : allTasks) {
			final long startDate = t.getStartExecution().getTime();
			final long endDate = t.getEndExecution().getTime();
			final long period = endDate - startDate;
			periodsList.add(period);
		}

		final Collection<Long> periods = periodsList;
		return periods;
	}
	private Double avgPeriodTask(final Dashboard d, final Collection<Long> periods) {
		long sum = 0L;

		for (final Long p : periods) {
			sum = sum + p;
		}

		final Double average = (double) (sum / periods.size());
		final Double avgDays = average / (8.64e7); //(1000 * 60 * 60 * 24)

		d.setAverageTaskExecutionPeriod(average);

		return avgDays;
	}

	private Double deviationPeriodTask(final Dashboard d, final Collection<Long> periods) {

		final double avg = d.getAverageTaskExecutionPeriod();

		Double variance = 0.;

		for (final Long p : periods) {
			Double range;
			range = Math.pow(p - avg, 2f);
			variance = variance + range;
		}

		variance = variance / periods.size();

		final Double deviation = Math.sqrt(variance);

		final Double devDays = deviation / (8.64e7);

		return devDays;
	}

	Double minimunTaskExecutionPeriod(final Collection<Long> periods) {
		final Long min = Collections.min(periods);

		return min / 8.64e7;
	}

	Double maximunTaskExecutionPeriod(final Collection<Long> periods) {
		final Long max = Collections.max(periods);

		return max / 8.64e7;
	}

	BigDecimal avgWorkloadTask() {
		final Collection<BigDecimal> workloads = this.repository.allWorkloads();
		BigDecimal sum = BigDecimal.ZERO; 
		BigDecimal parteEntera = new BigDecimal("0.00"); //Inicializo 0 horas
		BigDecimal parteDecimal = new BigDecimal("0.00"); //Inicializo 0 minutos
		
		for (final BigDecimal w : workloads) { // sumo las horas y minutos por separado
			parteEntera = parteEntera.add(w.setScale(0, RoundingMode.FLOOR));
			final BigDecimal aux = w.setScale(0, RoundingMode.FLOOR);
			parteDecimal = parteDecimal.add(w.subtract(aux));
		}
		
		parteDecimal = parteDecimal.multiply(new BigDecimal("100."));
		
		final BigDecimal aux = parteDecimal.divide(new BigDecimal("60."), AdministratorDashboardShowService.mc);
		BigDecimal horas = aux.setScale(2, RoundingMode.FLOOR);
		final BigDecimal minutos = aux.subtract(horas).multiply(new BigDecimal("60.")).divide(new BigDecimal("100.", AdministratorDashboardShowService.mc));
		horas = horas.add(parteEntera).add(minutos);
		sum = horas.add(minutos);
		BigDecimal average = sum.divide(new BigDecimal(workloads.size()), AdministratorDashboardShowService.mc);
		parteEntera = average.setScale(0, RoundingMode.FLOOR); 
		
		parteDecimal = average.subtract(parteEntera);
		
		parteDecimal = parteDecimal.multiply(new BigDecimal("0.60"), AdministratorDashboardShowService.mc);
		
		average = parteEntera.add(parteDecimal);
		
		return average;
	}
	
	BigDecimal devTaskWorkload(final Dashboard d) {
		
		final BigDecimal average = d.getAverageTaskWorkloads();
		BigDecimal variance = new BigDecimal("0.00");
		final Collection<BigDecimal> workloads = this.repository.allWorkloads();
		
		for(final BigDecimal w : workloads) {
			Double range;
			range = Math.pow(w.subtract(average).doubleValue(), 2f);
			variance = variance.add(BigDecimal.valueOf(range));
		}
		
		variance = variance.divide(BigDecimal.valueOf(workloads.size()));

		final BigDecimal deviation = BigDecimal.valueOf(Math.sqrt(variance.doubleValue())).setScale(2, RoundingMode.HALF_UP);

		return deviation;
	}


}
