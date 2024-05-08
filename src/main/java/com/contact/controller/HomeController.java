package com.contact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.dao.UserRepository;
import com.contact.entities.User;
import com.contact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "ContactSphere");
		return "home";
	}

	@RequestMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register-ContactSphere");
		m.addAttribute("user", new User());
		return "signup"; // Ensure this returns "signup"
	}

	// handler for user registration
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult res,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model m,
			HttpSession session) {

		try {
			if (!agreement) {
				session.setAttribute("message", new Message("Please accept the terms and conditions!", "alert-danger"));
				return "signup";
			}

			if (res.hasErrors()) {
				System.out.println("Error" + res.toString());
				m.addAttribute("user", user);
				return "signup";
			}
			// Continue with user registration process
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			User result = this.userRepository.save(user);
			m.addAttribute("user", new User());

			session.setAttribute("message", new Message("Successfully registered!!!", "alert-success"));
			return "signup";

		} catch (DataIntegrityViolationException e) {
			e.printStackTrace(); // Log the exception
			session.setAttribute("message", new Message("Email is already registered!", "alert-danger"));
			return "signup";
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception
			session.setAttribute("message",
					new Message("Something went wrong! Please try again later.", "alert-danger"));
			return "signup";
		}
	}

	// handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model m) {
		m.addAttribute("title", "Login-ContactSphere");
		return "login";
	}

}
