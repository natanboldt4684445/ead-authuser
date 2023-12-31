package com.ead.authuser.controllers;


import com.ead.authuser.clients.CourseClient;
import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.UserCourseDto;
import com.ead.authuser.models.UserCourseModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserCourseService;
import com.ead.authuser.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserCourseController {

    @Autowired
    CourseClient courseClient;

    @Autowired
    UserService userService;

    @Autowired
    UserCourseService userCourseService;

    @GetMapping("users/{userId}/courses")
    public ResponseEntity<Page<CourseDto>> getAllCoursesByUser(@PathVariable(value = "userId") UUID userId, @PageableDefault(sort = "courseId") Pageable pageable) {
        return ResponseEntity.ok().body(courseClient.getAllCoursesByUser(userId, pageable));
    }

    @PostMapping("users/{userId}/courses/subscription")
    public ResponseEntity<Object> savSubscriptionUserInCourse(@PathVariable(value = "userId") UUID userId, @RequestBody @Valid UserCourseDto userCourseDto) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found.");
        }
        if(userCourseService.existByUserAndCourseId(userModelOptional.get(), userCourseDto.getCourseId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: subscription already exists!");
        }
        UserCourseModel userCourseModel = userCourseService.save(userModelOptional.get().convertUserCourseModel(userCourseDto.getCourseId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(userCourseModel);
    }

    @DeleteMapping("users/courses/{courseId}")
    public ResponseEntity<Object> deleteUserCourseByCourseId(@PathVariable(value = "courseId") UUID courseId) {
        if (!userCourseService.existsByCourseId(courseId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserCourse not found.");
        }
        userCourseService.deleteUserCourseByCourseId(courseId);
        return ResponseEntity.ok().body("UserCourse deleted successfully");
    }

}
