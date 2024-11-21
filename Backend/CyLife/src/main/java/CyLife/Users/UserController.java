package CyLife.Users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import CyLife.Clubs.Club;
import CyLife.Clubs.ClubRepository;
import CyLife.Organisation.Organisation;
import CyLife.Organisation.OrganisationRepository;

import org.springframework.transaction.annotation.Transactional;
//Imports Dhvani Added
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//Only these two above

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    // Endpoint to get all users
    @GetMapping(path = "/users")
    List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Endpoint to get a user by id
    @GetMapping("/user/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepository.findById(id);

            if (user != null) {
                response.put("user", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "User not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint to update user by id
    @PutMapping("/update/{id}")
    public Map<String, Object> updateUser(
            @PathVariable int id, @RequestBody User updatedUser) {
        Map<String, Object> response = new HashMap<>();
        try {
            User existingUser = userRepository.findById(id);
            if (existingUser == null) {
                response.put("message", "User not found with id: " + id);
                response.put("status", "404");
                return response;
            }
            if (updatedUser.getName() != null) {
                existingUser.setName(updatedUser.getName());
            }
            if (updatedUser.getEmail() != null) {
                existingUser.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getPassword() != null) {
                existingUser.setPassword(updatedUser.getPassword());
            }
            if (updatedUser.getType() != null) {
                existingUser.setType(updatedUser.getType());
            }

            userRepository.save(existingUser);
            response.put("message", "User updated successfully.");
            response.put("status", "200");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Internal Server Error: " + e.getMessage());
            response.put("status", "500");
        }
        return response;
    }

    @Transactional
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteUser(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!userRepository.existsById(id)) {
                response.put("message", "User not found with id: " + id);
                response.put("status", "404");
            } else {
                userRepository.deleteById(id);
                response.put("message", "User deleted successfully.");
                response.put("status", "200");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Internal Server Error: " + e.getMessage());
            response.put("status", "500");
        }
        return response;
    }


    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestBody User newUser) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userRepository.existsByEmail(newUser.getEmail())) {
                response.put("message", "User already exists.");
                response.put("status", "409");
            } else {
                if (newUser.getType() == null) {
                    newUser.setType(User.User.UserType.STUDENT);
                    newUser.setType(User.UserType.STUDENT);
                }
                userRepository.save(newUser);
                response.put("message", "User registered successfully.");
                response.put("status", "201");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Internal Server Error: " + e.getMessage());
            response.put("status", "500");
        }
        return response;
    }

    @PostMapping({"/login"})
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap();
        String email = (String)credentials.get("email");
        String password = (String)credentials.get("password");
        if (email != null && password != null) {
            User user = this.userRepository.findByEmail(email.trim());
            if (user != null && user.getPassword().equals(password.trim())) {
                response.put("message", "Login successful");
                response.put("userType", user.getType());
                response.put("userID", user.getUserId());
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Invalid email or password.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } else {
            response.put("message", "Email or password is missing.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping({"/user/{userId}/addClub/{clubId}"})
    public ResponseEntity<Map<String, Object>> addUserToClub(@PathVariable int userId, @PathVariable int clubId) {
        Map<String, Object> response = new HashMap();

        try {
            User user = this.userRepository.findById(userId);
            Club club = (Club)this.clubRepository.findById(clubId).orElse((Object)null);
            if (user == null) {
                response.put("message", "User not found with id: " + userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (club == null) {
                response.put("message", "Club not found with id: " + clubId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                user.setClub(club);
                this.userRepository.save(user);
                response.put("message", "User added to club successfully.");
                return ResponseEntity.ok(response);
            }
        } catch (Exception var6) {
            var6.printStackTrace();
            response.put("message", "Internal Server Error: " + var6.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
