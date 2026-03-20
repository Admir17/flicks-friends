package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Invite;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.repository.InviteRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.WatchPartyRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import jakarta.mail.*;
import jakarta.mail.internet.*;

@Service
public class WatchPartyService {
    private static final Logger log = LoggerFactory.getLogger(WatchPartyService.class);

    private final WatchPartyRepository watchPartyRepository;
    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;

    @Value("${app.backend.base-url}")
    private String baseUrl;
    @Value("${spring.mail.username:}")
    private String smtpUser;
    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Autowired
    public WatchPartyService(WatchPartyRepository watchPartyRepository,
            UserRepository userRepository,
            InviteRepository inviteRepository) {
        this.watchPartyRepository = watchPartyRepository;
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
    }

    // Create a new watch party
    public WatchParty createWatchParty(User organizer, String title, String contentLink, String description,
            LocalDateTime scheduledTime) {
        if (organizer == null || scheduledTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organizer and scheduled time are required.");
        }
        ZonedDateTime scheduledTimeUTC = scheduledTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime scheduledTimeLocal = scheduledTimeUTC.withZoneSameInstant(ZoneId.systemDefault());
        if (scheduledTimeLocal.isBefore(ZonedDateTime.now(ZoneId.systemDefault()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scheduled time must be in the future.");
        }

        WatchParty watchParty = new WatchParty();
        watchParty.setOrganizer(organizer);
        watchParty.setTitle(title);
        watchParty.setContentLink(contentLink);
        watchParty.setDescription(description);
        watchParty.setScheduledTime(scheduledTime);

        return watchPartyRepository.save(watchParty);
    }

    // Get watch parties by organizer ID
    public List<WatchParty> getWatchPartiesByOrganizer(Long organizerId) {
        return watchPartyRepository.findByOrganizer_Id(organizerId);
    }

    // Get all watch parties
    public List<WatchParty> getAllWatchParties() {
        return watchPartyRepository.findAll();
    }


    // Invite a user to a watch party
    public String inviteUserToWatchParty(Long watchPartyId, String username, Long inviterId) {
        log.info("Attempting to invite username={} to watchPartyId={}", username, watchPartyId);

        // Fetch user from the repository
        User userToInvite = userRepository.findByUsername(username);

        if (userToInvite == null) { // Adjusted to handle null return
            log.warn("User not found in database: {}", username);
            return "Username does not exist"; // User not found
        }

        log.debug("User found: username={}, email={}", userToInvite.getUsername(), userToInvite.getEmail());

        // Create a new invite object
        Invite invite = new Invite();
        invite.setWatchPartyId(watchPartyId);
        invite.setUsername(username);
        invite.setStatus("pending");

        // Save invite to the repository
        inviteRepository.save(invite);
        log.info("Invite persisted for username={} watchPartyId={}", username, watchPartyId);

        // Send email invite using the user's email
        sendInviteEmail(userToInvite.getEmail(), watchPartyId, username);
        log.info("Invite email processing finished for {}", userToInvite.getEmail());

        return "Invite sent successfully!";
    }


    //  Send email invite via SMTP
    private void sendInviteEmail(String email, Long watchPartyId, String username) {
        if (smtpUser == null || smtpUser.isBlank() || smtpPassword == null || smtpPassword.isBlank()) {
            log.warn("SMTP credentials are not configured. Invite email skipped for {}", email);
            return;
        }

        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String acceptLink = baseUrl + "/api/watchparties/" + watchPartyId + "/invite-response?username="
                + encodedUsername + "&status=accepted";
        String declineLink = baseUrl + "/api/watchparties/" + watchPartyId + "/invite-response?username="
                + encodedUsername + "&status=declined";

        String messageBody = "You've been invited to a watch party!\n\nClick below to respond:\n" +
                "✅ Accept: " + acceptLink + "\n" +
                "❌ Decline: " + declineLink;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(smtpUser, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUser));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("You're Invited to a Watch Party!");
            message.setText(messageBody);

            jakarta.mail.Transport.send(message);
        } catch (MessagingException e) {
            log.error("Invite email failed for {}: {}", email, e.getMessage());
        }
    }

    // Fetch list of invited users
    public List<String> getInvitedUsers(Long watchPartyId) {
        return inviteRepository.findByWatchPartyId(watchPartyId)
                .stream()
                .map(Invite::getUsername)
                .toList();
    }

    // Update invite response status
    public boolean updateInviteStatus(Long watchPartyId, String username, String status) {
        List<Invite> invites = inviteRepository.findByWatchPartyIdAndUsername(watchPartyId, username);

        if (!invites.isEmpty()) {
            for (Invite invite : invites) {
                invite.setStatus(status);
                inviteRepository.save(invite);
            }
            return true;
        }
        return false;
    }

    // Fetch latest invite responses
    public List<String> getLatestInviteResponses(Long watchPartyId) {
        return inviteRepository.findByWatchPartyId(watchPartyId)
                .stream()
                .map(invite -> invite.getUsername() + " - " + invite.getStatus())
                .toList();
    }

    public WatchParty getWatchPartyById(Long id) {
        return watchPartyRepository.findById(id)
                .orElseThrow(()
                        -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch-party not found."));
    }

    public List<WatchParty> getWatchPartiesForInvitee(String username) {
        return inviteRepository.findByUsernameAndStatus(username, "accepted")
                .stream()
                .map(inv -> watchPartyRepository.findById(inv.getWatchPartyId())
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }
}
