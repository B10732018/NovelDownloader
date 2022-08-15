package ND;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ND {
    private JFrame frame;
    private JTextField host;
    private TextArea log;
    private JButton download;

    public ND() {
        frame = new JFrame();
    }

    public void run() {
        frame.setSize(600, 300);
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("host     ");
        GridBagConstraints clabel = new GridBagConstraints();
        clabel.gridx = 0;
        clabel.gridy = 0;
        clabel.gridwidth = 1;
        clabel.gridheight = 1;
        clabel.weightx = 0;
        clabel.weighty = 0;
        clabel.fill = GridBagConstraints.NONE;
        clabel.anchor = GridBagConstraints.WEST;
        frame.add(label, clabel);

        host = new JTextField();
        host.setPreferredSize(new Dimension(200, 30));
        GridBagConstraints chost = new GridBagConstraints();
        chost.gridx = 1;
        chost.gridy = 0;
        chost.gridwidth = 6;
        chost.gridheight = 1;
        chost.weightx = 0;
        chost.weighty = 0;
        chost.fill = GridBagConstraints.BOTH;
        chost.anchor = GridBagConstraints.WEST;
        frame.add(host, chost);

        download = new JButton("download");
        GridBagConstraints cdownload = new GridBagConstraints();
        cdownload.gridx = 0;
        cdownload.gridy = 1;
        cdownload.gridwidth = 2;
        cdownload.gridheight = 1;
        cdownload.weightx = 0;
        cdownload.weighty = 0;
        cdownload.fill = GridBagConstraints.BOTH;
        cdownload.anchor = GridBagConstraints.CENTER;
        frame.add(download, cdownload);

        log = new TextArea("");
        GridBagConstraints clog = new GridBagConstraints();
        clog.gridx = 0;
        clog.gridy = 2;
        clog.gridwidth = 6;
        clog.gridheight = 1;
        clog.weightx = 0;
        clog.weighty = 0;
        clog.fill = GridBagConstraints.NONE;
        clog.anchor = GridBagConstraints.WEST;
        log.setEditable(false);
        frame.add(log, clog);

        download.addActionListener(new Downloader());

        frame.setVisible(true);
    }

    public class Downloader implements ActionListener {
        public Downloader() {

        }

        @Override
        public void actionPerformed(ActionEvent event) {
            String path;
            JFileChooser J_File_Chooser = new JFileChooser();
            J_File_Chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = J_File_Chooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                path = J_File_Chooser.getSelectedFile().getAbsolutePath();
                String url = host.getText();
                System.out.println(url);
                System.out.println("directory: " + path);
                (new Thread() {
                    @Override
                    public void run() {

                        try {
                            download.setBackground(Color.RED);
                            download.setEnabled(false);
                            download(url, path);
                        } catch (Exception e) {
                            download.setEnabled(true);
                            download.setBackground(Color.GREEN);
                            e.printStackTrace();
                            log.append(e.toString() + "\n");
                        }
                    }
                }).start();
            } else {
                System.out.println("cancel");
                log.append("cancel\n");
            }

        }

        private void download(String s, String p) throws Exception {
            String title = "tmp";
            File file;
            FileOutputStream fop;
            OutputStreamWriter writer;
            String next_url;

            Document doc = Jsoup.connect(s).userAgent(
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0")
                    .get();
            Elements content = doc.getElementsByClass("content");

            title = doc.title();
            Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
            Matcher matcher = pattern.matcher(title);
            title = matcher.replaceAll(" ");
            file = new File(p.replace("/", "\\") + "\\" + title + ".txt");
            if (file.exists()) {
                log.append("file has already existed\n");
                download.setEnabled(true);
                download.setBackground(Color.GREEN);
                return;
            } else {
                file.createNewFile();
                fop = new FileOutputStream(file);
                writer = new OutputStreamWriter(fop, "UTF-8");
            }

            writer.append(content.html().replace("<br>", "\n"));
            System.out.println(p.replace("/", "\\") + "\\" + title + ".txt");
            log.append("Finish: " + title + "\n");

            while (!doc.getElementsByClass("next-chapter").isEmpty()) {
                next_url = doc.getElementsByClass("next-chapter").attr("href");
                if (next_url.indexOf("https:") != 0) {
                    next_url = "https:" + next_url;
                }
                System.out.println(next_url);

                doc = Jsoup.connect(next_url).userAgent(
                        "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0")
                        .get();
                content = doc.getElementsByClass("content");

                writer.append("\n\n\n\n" + content.html().replace("<br>", "\n"));
                System.out.println(p.replace("/", "\\") + "\\" + title + ".txt");
                log.append("Finish: " + doc.title() + "\n");

            }
            writer.close();
            fop.close();

            download.setEnabled(true);
            download.setBackground(Color.GREEN);
        }
    }
}
