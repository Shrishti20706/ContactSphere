package com.contact.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.dao.UserRepository;
import com.contact.entities.User;
import com.contact.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	Random random = new Random();

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	// email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {

		return "forgot_email_form";

	}

	@RequestMapping("/sendotp")
	public String sendOtp(@RequestParam("email") String email, HttpSession s) {

		// generating otp of 4 digit

		int otp = random.nextInt(999999);
		System.out.println("otp" + otp);

		String subject = "OTP from ContactSphere";
		String message = "" + "<div style='border:1px solid black; padding:20px;background-color:#e2e2e2'>" + "<h1>"
				+ " otp is " + "<b>" + otp + "</b>" + "</n>" + "</h1>" + "</div>";
		String to = email;

		boolean flag = this.emailService.sendEmail(subject, message, to);
		if (flag == true) {
			s.setAttribute("myotp", otp);
			s.setAttribute("email", email);
			return "verifyotp";

		} else {
			s.setAttribute("message", " check your email id !!!");
			return "forgot_email_form";
		}

	}

	// verify otp
	@PostMapping("/verifyotp")
	public String VerifyOtp(@RequestParam("otp") int otp, HttpSession s) {
		int myOtp = (int) s.getAttribute("myotp");
		String email = (String) s.getAttribute("email");

		if (myOtp == otp) {

			// change password
			User user = this.userRepository.getUserByUserName(email);
			if (user == null) {

				// send error
				s.setAttribute("message", " User doesn't exist!!! please check your email id !!!");
				return "forgot_email_form";
			} else {
				// change password form
			}
			return "passwordchange";
		}

		else {
			s.setAttribute("message", "You have entered wrong otp!!!");

			return "verifyotp";

		}
	}
	// change password

	@PostMapping("/changepass")
	public String changepassword(@RequestParam("newpassword") String newpassword, HttpSession s) {

		String email = (String) s.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);

		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));

		this.userRepository.save(user);

		return "redirect:/signin?change=password change successfully";
	}
}
