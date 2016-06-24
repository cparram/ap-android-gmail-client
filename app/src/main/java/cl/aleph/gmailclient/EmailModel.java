package cl.aleph.gmailclient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 6/24/16.
 */
public class EmailModel {
    public static int IMAP = 0;
    public static int POP3 = 1;

    private String from;
    private String subject;
    private String date;

    public EmailModel(String from, String subject, String date) {
        this.from = from;
        this.subject = subject;
        this.date = date;
    }

    /**
     *
     * @param count
     * @param protocol
     * @return
     */
    public static List<EmailModel> getHeaderEmails(int count, int protocol) {
        List<EmailModel> emails  = new ArrayList<>(count);
        // dummy values
        emails.add(new EmailModel("cesar.parra@alumnos.usm.cl", "Saludos", "fecha"));
        emails.add(new EmailModel("cesar@parra.cl", "Saludos2", "fecha"));
        return emails;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }
}
