package com.contact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contact.dao.UserRepo;
import com.contact.entity.User;
import com.contact.helper.Messege;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepo userRepo ;
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home-Smart contact Manager ");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About-Smart contact Manager ");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Resgister-Smart contact Manager ");
		model.addAttribute("user", new User());
		return "signup";
	}

	// this handler for registering user ;
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, @RequestParam("myImageUrl") MultipartFile myImageUrl,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement,Model model, HttpSession session)  {
		try {
			if(!agreement) {
				System.out.println("you are not checked terms and conditions");
				//throwing the exception customize ;
			throw new Exception("you are not checked terms and conditions");
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			//rewrite the image to the img folder ;
			File savefile = new ClassPathResource("static/img").getFile();
			// using Paths class for the getting the path of the file where uploaded ;
			Path path = Paths.get(savefile.getAbsolutePath() + File.separator + myImageUrl.getOriginalFilename());

			Files.copy(myImageUrl.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING); // very imp.
			
			//set the image to the user ;
			System.out.println("User Image Uploaded successfully...");
			System.gc();
			//photo placeHolder;
			if(myImageUrl.isEmpty()) {
				user.setImageUrl("placeholderdp.png");
				
			}
			user.setImageUrl(myImageUrl.getOriginalFilename());
			user.setPassword(passwordEncoder.encode(user.getPassword()));//encoded password saved by this method 
			
			
			System.out.println(agreement);
			System.out.println(user);
			
	        User savedUser = userRepo.save(user);
	        model.addAttribute("user", new User());
	       session.setAttribute("message",new Messege("Successfully registerd ! !", "alert-success"));
	       // System.out.println("_______________________________________________________________________________________");
	        System.out.println(savedUser);
	        
	        return "signup";

		} catch (Exception e) {
		 e.printStackTrace();
		 model.addAttribute("user" ,user);
		session.setAttribute("message",new Messege("something went wrong ! !"+e.getMessage(),"alert-danger"));
		 return "signup";
		}
		
	}
     

	//handler for custom login Page ;
	@GetMapping("/login")
	 public String customLogin(Model model) {
		model.addAttribute("title" ,"Login in Page");
		 
		 
		 return "login";
	 }
	
	
	
}
