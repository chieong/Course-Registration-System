package org.cityuhk.CourseRegistrationSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.cityuhk.CourseRegistrationSystem.Service.ICredentialRepository;
import org.cityuhk.CourseRegistrationSystem.Service.SessionManager;
import org.junit.jupiter.api.Test;

class SessionManagerTest {
	
    @Test
    void CreatesNewSessionTest() {
        class StubCredentialRepository implements ICredentialRepository {
            @Override
            public boolean validateCredential(String userEID, String userPassword) {
                   return true;
            }
        }
        ICredentialRepository credentialRepository = new StubCredentialRepository();
        SessionManager sessionManager = new SessionManager(credentialRepository);
        String username = "user1";
        UUID session = sessionManager.createNewSession(username, "1");
        assertEquals(username, sessionManager.getSessionUserEID(session));
    }
    
    @Test
    void FailedCreatesNewSessionTest() { 
        class StubCredentialRepository implements ICredentialRepository {
            @Override
            public boolean validateCredential(String userEID, String userPassword) {
                   return false;
            }
        }
        ICredentialRepository credentialRepository = new StubCredentialRepository();
        SessionManager sessionManager = new SessionManager(credentialRepository);
        String username = "user1";
        UUID session = sessionManager.createNewSession(username, "1");
        assertEquals(null, session);
    }
    
}
