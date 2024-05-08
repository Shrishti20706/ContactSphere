package com.contact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.contact.dao.ContactRepository;
import com.contact.dao.UserRepository;
import com.contact.entities.Contact;
import com.contact.entities.User;
import com.contact.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// to get common data
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String userName = principal.getName();
		System.out.println("username" + userName);
		// get user using username(Email)

		User user = userRepository.getUserByUserName(userName);
		System.out.println("user" + user);
		m.addAttribute("user", user);

	}

	// dashboard home
	@RequestMapping("/index")
	public String dasahboard(Model m, Principal principal) {

		m.addAttribute("title", "User Dashboard");

		return "normal/user_dashboard";
	}

	// open add form handler
	@RequestMapping("/add_contact")
	public String openAddcontactForm(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/add_contact";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			if (file.isEmpty()) {
				System.out.println("inserted file is empty");
				contact.setImage("user.png");
			} else {
				// update name of image to contact
				contact.setImage(file.getOriginalFilename());
				// add image file to particular folder
				File savefile = new ClassPathResource("static/image").getFile();
				Path p = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), p, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image uploaded successfully");
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("added to database");
			System.out.println("data" + contact);

			// success message
			session.setAttribute("message", new Message("contact added !!!", "success"));

		} catch (Exception e) {
			// TODO: handle exception

			System.out.println("Error" + e.getMessage());
			e.printStackTrace();

			// error message
			session.setAttribute("message", new Message("something went wrong !!!", "danger"));

		}

		return "normal/add_contact";
	}

	// show contact handler
	@GetMapping("/showcontact/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "Show Contact");

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		Pageable pg = PageRequest.of(page, 7);

		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pg);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentpage", page);
		m.addAttribute("totalpage", contacts.getTotalPages());

		return "normal/show_contact";

	}

	// showing particular contact details
	@RequestMapping("/{cID}/contact")
	public String showContactDetail(@PathVariable("cID") Integer cID, Model m, Principal p) {
		System.out.println("cID" + cID);

		Optional<Contact> contactOptional = this.contactRepository.findById(cID);
		Contact contact = contactOptional.get();

		// to get the person who is login using principal
		String userName = p.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());

		}
		return "normal/contact_detail";
	}

	// delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cID, Model m, Principal p, HttpSession session) {
		Contact contact = this.contactRepository.findById(cID).get();

		String userName = p.getName();
		User user = this.userRepository.getUserByUserName(userName);

		int id = user.getId();
		int id1 = contact.getUser().getId();

		// **** before deleting contact unlinked it from user
		contact.setUser(null);

		if (id == id1) {
			// this.contactRepository.delete(contact);
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Contact deleted successfully...", "success"));
		} else {
			session.setAttribute("message", new Message("dont have access to delete this contact...", "danger"));

		}

		return "redirect:/user/showcontact/0";
	}

	// Open update form handler
	@PostMapping("/updatecontact/{cID}")
	public String updateForm(@PathVariable("cID") Integer cID, Model m) {
		m.addAttribute("title", "update contact");
		Contact contact = this.contactRepository.findById(cID).get();

		m.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal p) {
		try {
			// fetch old contact detail to fetch old image

			Contact oldcontactDetail = this.contactRepository.findById(contact.getcID()).get();

			// image handling
			if (!file.isEmpty()) {
				// if file exist means image has to be rewrite

				// first delete old pic
				File delfile = new ClassPathResource("static/image").getFile();
				File file1 = new File(delfile, oldcontactDetail.getImage());

				file1.delete();
				// update new photo

				File savefile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldcontactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(p.getName());

			contact.setUser(user);

			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your contact updated successfully", "success"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		System.out.println("contact name" + contact.getName());
		System.out.println("contact name" + contact.getName());
		return "redirect:/user/" + contact.getcID() + "/contact";
	}

	// your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title", "Profile page");
		return "normal/profile";

	}

	// open settings handler
	@GetMapping("/settings")
	public String openSettingd() {

		return "normal/settings";
	}

	// change password handler
	@PostMapping("/changepassword")
	public String changepassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal p, HttpSession session) {

		System.out.println("old password" + oldPassword);
		System.out.println("new password" + newPassword);

		String UserName = p.getName();
		User currentUser = this.userRepository.getUserByUserName(UserName);

		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			// if matches then change the password

			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);

			session.setAttribute("message", new Message("your password is successfully changed", "success"));

		} else {
			session.setAttribute("message", new Message("your old password is wrong", "danger"));
			return "redirect:/user/settings";

		}

		return "redirect:/user/index";
	}

	// creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder() {
		System.out.println("done");
		return "done";
	}
}
