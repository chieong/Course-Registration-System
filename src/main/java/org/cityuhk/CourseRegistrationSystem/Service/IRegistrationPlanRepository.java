public interface IRegistrationPlanRepository {

	/**
	 * 
	 * @param rp
	 */
	int addPlanRecord(RegistrationPlan rp);

	/**
	 * 
	 * @param planId
	 */
	RegistrationPlan getPlanById(int planId);

}