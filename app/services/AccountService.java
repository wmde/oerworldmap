package services;

public interface AccountService {
  String getProfileId(String username);
  String getUsername(String profileId);
}
