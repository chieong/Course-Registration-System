package org.cityuhk.course_registration_system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.UUID;

import org.cityuhk.CourseRegistrationSystem.Service.Course;
import org.cityuhk.CourseRegistrationSystem.Service.Section;
import org.junit.jupiter.api.Test;

public class CourseTest {
	
    @Test
    void GetCreditsTest() {
    	Course course=new Course("abc","abc",10,"abc","abc",null,null,null);
    	assertEquals(10,course.getCredits());
    }
    
    @Test 
    void SearchSectionIDTest() {
    	ArrayList<Section> sec=new ArrayList<>();
    	Section s=new Section(9,0,0,0,null,null,null,0,null,null);
    	sec.add(s);
    	Course c=new Course(null,null,0,null,null,null,null,sec);
    	assertEquals(s,c.SearchSectionID(9));
    }
    
    @Test
    void FailedSearchSectionIDTest() {
    	ArrayList<Section> sec=new ArrayList<>();
    	Section s=new Section(9,0,0,0,null,null,null,0,null,null);
    	sec.add(s);
    	Course c=new Course(null,null,0,null,null,null,null,sec);
    	assertEquals(null,c.SearchSectionID(10));
    }
}

