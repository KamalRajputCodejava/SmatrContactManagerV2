package com.contact.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contact.dao.ContactRepo;
import com.contact.dao.UserRepo;
import com.contact.entity.Contact;
import com.contact.entity.User;
import com.contact.helper.Messege;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private ContactRepo contactRepo;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder ;

	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {

		String username = principal.getName(); // Principal interface is used for the getting the
		// data from the html page ;
		System.out.println("USERNAME  :" + username);

		// get the user from the user name ;
		User user = userRepo.getUserByUserName(username);
		System.out.println("USER  :" + user);
		m.addAttribute("user", user);

	}

//dashboard home 
	@RequestMapping("/index")
	public String dashBoard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

//open add form handler ;
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// processing at contact form ;;
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepo.getUserByUserName(name);

			// processing and uploading file ;
			if (file.isEmpty()) {
				System.out.println("Empty file..");
				contact.setImage("placeholderContact.png");
			} else {
				// upload file after
				contact.setImage(file.getOriginalFilename());
				// getting the upload file directory in which we want to upload file ;
				File savefile = new ClassPathResource("static/img").getFile();
				// using Paths class for the getting the path of the file where uploaded ;
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING); // very imp.

				System.out.println("file uploaded successfully..");
				System.gc(); // very imp. when we use the springbootv blow v4.0 ;

			}

			// add the contacts into the list of the contact which have user ;
			user.getContacts().add(contact);

			// contact main user add kiya.
			contact.setUser(user);

			// save the contact in db
			this.userRepo.save(user);
			System.out.println("DATA  :" + contact);
			System.out.println("saved the contacts save db");
			// message success to the add_contact_page;
			session.setAttribute("message", new Messege("contact saved successfully..", "success"));

		} catch (IOException e) {
			System.out.println("ERROR :" + e.getMessage());
			e.printStackTrace();
			// error message ;
			session.setAttribute("message", new Messege("something went wrong! Try again!", "danger"));

		}

		return "normal/add_contact_form";
	}
//show contacts handler ;
	// per page five contacts 5[n]
	// current page =0[page];

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "Show Contacts");
		// contact ki list ko bhejna hai jo show karani hai
		String userName = principal.getName();
		User user = this.userRepo.getUserByUserName(userName);
		// current page and contact per page is 5 ;
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepo.findContactsByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// showing particular contact details;
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("cId :" + cId);
		Optional<Contact> contactop = this.contactRepo.findById(cId);
		Contact contact = contactop.get();
		// checking the condition of authorize user ;
		String userName = principal.getName();
		User user = this.userRepo.getUserByUserName(userName);
		if (user.getId() == contact.getUser().getId()) // very important
		{
			model.addAttribute("model", contact);
		}

		return "normal/contact_detail";

	}

//delete contact handler ;;
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Principal principal, HttpSession session) throws IOException {

		Contact contactop = this.contactRepo.findById(cId).get();
		String userName = principal.getName();
		User user = userRepo.getUserByUserName(userName);

		user.getContacts().remove(contactop);
		this.userRepo.save(user);

		// remove
		// img folder se by the code
		      
		File deleteFile = new ClassPathResource("static/img").getFile();
		File file1 = new File(deleteFile , contactop.getImage());
		file1.delete();
		
		// contact.getImage() se image mil jaayegi ;
		// this.contactRepo.delete(contact);

		session.setAttribute("message", new Messege("Contact Deleted successfully..", "alert-success"));

		return "redirect:/user/show-contacts/0";
	}

//open update form Handler
	@PostMapping("/update-contact/{cid}")
	public String updateContactForm(Model model, @PathVariable("cid") Integer cId) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepo.findById(cId).get();

		model.addAttribute("contact", contact);

		return "normal/updateform";
	}

//process-update handler ;
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateProcessHandler(@ModelAttribute Contact contact, Model m, HttpSession session,
			@RequestParam("profileImage") MultipartFile file, Principal principal) {
		try {

			// old contact details ;
			Contact oldContactDetails = this.contactRepo.findById(contact.getcId()).get();

			// check image
			if (!file.isEmpty()) {
				// file work
				// rewrite

				// delete old picture very important operation by which we delete the file from
				// the img folder o given folder
				// old method of delete file

				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();

				// update new picture
				File savefile = new ClassPathResource("static/img").getFile();
				// using Paths class for the getting the path of the file where uploaded ;
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING); // very imp.

				contact.setImage(file.getOriginalFilename());

			} else {
				// old contact details main setprofileImage();
				contact.setImage(oldContactDetails.getImage());
			}

			User user = this.userRepo.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepo.save(contact);

			session.setAttribute("message", new Messege("Your Contact is updated Successfully..", "alert-success"));

		} catch (Exception e) {
			// handle exception
		}

		// console check break
		System.out.println("Contact Name:" + contact.getName());
		System.out.println("Contact Id:" + contact.getcId());

		return "redirect:/user/" + contact.getcId() + "/contact";
	}
//your profile handler ;;
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile Image");
		
	return "normal/profile";
	}
	//open setting handler ;;
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings";
	}
	//handler for change-password 
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldpassword") String oldpassword ,@RequestParam("newpassword") String newpassword ,
			HttpSession session,Principal principal) {
		
		System.out.println("old pass:"+oldpassword);
		System.out.println("new Pass:"+newpassword);
		//checking the old and new password ;
		String username = principal.getName();
		User currentUser = this.userRepo.getUserByUserName(username);
		
		if(this.bCryptPasswordEncoder.matches(oldpassword,currentUser.getPassword())) {
			//change password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newpassword));			
			this.userRepo.save(currentUser);
			session.setAttribute("message", new Messege("your password changed...","success"));
		}
		else {
			session.setAttribute("message",new Messege("your password not matched","danger"));
			return "redirect:/user/settings";
		}
		
		
		return "redirect:/user/index";
	}
	
	
	
}
