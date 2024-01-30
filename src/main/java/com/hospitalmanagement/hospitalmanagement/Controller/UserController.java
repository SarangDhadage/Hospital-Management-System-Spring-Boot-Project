package com.hospitalmanagement.hospitalmanagement.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hospitalmanagement.hospitalmanagement.Repository.ContactRepository;
import com.hospitalmanagement.hospitalmanagement.Repository.UserRepository;
import com.hospitalmanagement.hospitalmanagement.helper.Message;
import com.hospitalmanagement.hospitalmanagement.model.Contact;
import com.hospitalmanagement.hospitalmanagement.model.User;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    // method for adding data to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        System.out.println("USERNAME " + userName);

        // get the user using username(Email)

        User user = userRepository.getUserByUserName(userName);

        System.out.println("USER " + user);

        model.addAttribute("user", user);

    }

    // dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }

    // open add form handler
    @GetMapping("/add-patient")
    public String openAddPatientForm(Model model) {
        model.addAttribute("title", "Add Patient");
        model.addAttribute("contact", new Contact());
        return "normal/add_patient_form";
    }

    // processing add contact form
    @PostMapping("/process-patient")
    public String processPatient(
            @ModelAttribute Contact contact,
            @RequestParam("profileImage") MultipartFile file,
            Principal principal, HttpSession session) {

        try {

            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);

            // processing and uploading file

            if (file.isEmpty()) {
                System.out.println("File is Empty");
                contact.setImage("patient.png");

            } else {
                contact.setImage(file.getOriginalFilename());

                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image is Uploaded");
            }

            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
            System.out.println("DATA " + contact);

            System.out.println("Added to data base");

            session.setAttribute("message", new Message("Your Patient is added !! Add More", "success"));

        } catch (Exception e) {
            System.out.println("ERROR " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong !! Try again...", "danger"));

        }

        return "normal/add_patient_form";
    }

    // show patient handler
    @GetMapping("/show-patient")

    public String showPatient(Model m, Principal principal) {
        m.addAttribute("title", "Show patient");

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        List<Contact> contacts = this.contactRepository.findContactsByUser(user.getId());

        m.addAttribute("contacts", contacts);

        return "normal/show_patient";
    }

    // showing particular patient details

    @RequestMapping("/{cId}/contact")
    public String showPatientDetail(@PathVariable("cId") Integer cId, Model model, Principal principle) {
        System.out.println("CID " + cId);
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        String userName = principle.getName();
        User user = this.userRepository.getUserByUserName(userName);

        if (user.getId() == contact.getUser().getId())
            model.addAttribute("contact", contact);

        return "normal/patient_detail";
    }

    // delete contact handeler

    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session ,Principal principal) {
        System.out.println("CID " + cId);

        Contact contact = this.contactRepository.findById(cId).get();
        // check...
        
        User user = this.userRepository.getUserByUserName(principal.getName());
        user.getContacts().remove(contact);
        this.userRepository.save(user);

        System.out.println("DELETED");
        session.setAttribute("meassage", new Message("patient deleted succesfully", "success"));

        return "redirect:/user/show-patient";
    }

    // update form
    @PostMapping("/update-patient/{cid}")
    public String updateForm(@PathVariable("cid") Integer cid, Model m) {
        m.addAttribute("title", "update patient");

        Contact contact = this.contactRepository.findById(cid).get();
        m.addAttribute("contact", contact);
        return "normal/update_form";
    }

    // update patient
    @RequestMapping(value = "/process-update", method = RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
            Model m, HttpSession session, Principal principal) {

        try {

            Contact oldpatientDetails = this.contactRepository.findById(contact.getcId()).get();
            if (!file.isEmpty()) {

                // delete old photo
                File deleteFile = new ClassPathResource("static/img").getFile();
                File file1=new File(deleteFile,oldpatientDetails.getImage());
                file1.delete();

                // update new photo
                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                contact.setImage(file.getOriginalFilename());
            } else {
                contact.setImage(oldpatientDetails.getImage());
            }
            User user = this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message", new Message("Your Pateint data updated...", "success"));
        } catch (Exception e) {
            // TODO: handle exception
        }
        System.out.println("PATIENT NAME" + contact.getName());
        return "redirect:/user/" + contact.getcId() +"/contact";
    }

    //your profile handler
    @GetMapping("/profile")
    public String yourProfile(Model model)
    {
        model.addAttribute("title", "Profile Page");
        return "normal/profile";
    }

    
}
