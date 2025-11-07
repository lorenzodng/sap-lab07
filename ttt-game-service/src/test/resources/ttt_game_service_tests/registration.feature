Feature: User registration 
  As a new user
  I want to register 
  So that I can login and play games

  Scenario: Successful registration
    Given I am on the registration page
    And I have not registered before
    When I register with a unique username "eugenio" and a valid password "Secret#123"
    Then I should see a confirmation that my account was created

  Scenario: Registration fails with invalid data
    Given I am on the registration page
    And Someone already registered with a username "eugenio"
    When I register with username "eugenio" and any password
    Then I should see an error "Username already taken"
    And my account should not be created

 