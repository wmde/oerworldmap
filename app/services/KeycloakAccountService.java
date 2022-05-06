package services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import play.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fo
 */
public class KeycloakAccountService implements AccountService {

  public KeycloakAccountService(String aServerUrl, String aRealm, String aUsername, String aPassword, String aClientId,
    File aPermissionsDir) {
  }

  public boolean deleteUser(String username) {
    return false;
  }

  public String getProfileId(String username) {
    // XXX
    return "1";
  }

  public void setProfileId(String username, String profileId) {
    // XXX
  }

  public String getUsername(String profileId) {
    // XXX
    return "foo";
  }

}
