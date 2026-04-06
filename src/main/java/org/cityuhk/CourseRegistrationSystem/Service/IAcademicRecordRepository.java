public interface IAcademicRecordRepository {

	/**
	 * 
	 * @param studentId
	 */
	ArrayList<AcademicRecord> getStudentRecord(id studentId);

}