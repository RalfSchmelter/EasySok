package org.easysok;

import java.util.List;
import java.util.regex.Pattern;

/**
 * This class represents a sokoban level (which is a map plus some other properties).
 */
public class Level {

    /**
     * used for splitting authors and emails.
     */
    private static final Pattern author_email_regexp = Pattern.compile(",");

    /**
     * used for splitting emails.
     */
    private static final Pattern email_regexp = Pattern.compile("[<>()]");

    /**
     * The compressed map.
     */
    // CompressedMap compressed_map;

    /**
     * The map.
     */
    private Map map;

    /**
     * The authors.
     */
    private List<String> authors;

    /**
     * The email addresses of the authors.
     */
    private List<String> emails;

    /**
     * The homepage.
     */
    private String homepage;

    /**
     * The copyright.
     */
    private String copyright;

    /**
     * The name.
     */
    private String name;

    /**
     * The additional level info.
     */
    private String info;

    /**
     * The difficulty.
     */
    int difficulty;

    /**
     * Creates the level.
     *
     * @param map The map to use.
     * @param authors The author(s) the of the level.
     * @param email The email addresses of the authors (must be of the same size as authors).
     * @param homepage The homepage of the author.
     * @param copyright The copyright of the level.
     * @param name The name of the level.
     * @param info Additional info for this level.
     * @param difficulty The difficulty of the level in the range[-1:10], with 0 being trivially easy.
     *                   -1 means an unknown difficulty.
     */
    public Level(Map map, List<String> authors, List<String> emails, String homepage, String copyright, 
            String name, String info, int difficulty) {
        assert authors.size() == emails.size();
        
        this.map = map;
        this.authors = authors;
        this.emails = emails;
        this.homepage = homepage;
        this.copyright = copyright;
        this.name = name;
        this.info = info;
        this.difficulty = difficulty;
    }

    /**
     * Constructs the map from a list of lines in xsb format.
     *
     * The constructor removes all lines in the string list, which were before the
     * actual map and the lines of the actual map itself and all lines up to another
     * map (in these lines we also look for additional info like author etc.).
     * Be sure to call map.isValid() afterwards.
     *
     * Note that you have to supply default values for all author etc, which will only
     * be overwritten, if there exist other information for the level.
     *
     * @param lines The list with the lines.
     * @param authors The author(s) the of the level.
     * @param emails The email addresses of the authors.
     * @param homepage The homepage of the author.
     * @param copyright The copyright of the level.
     * @param info The additional information of the level.
     * @param difficulty The difficulty of the level in the range[-1:10], with 0 being trivially easy.
     *                   -1 means an unknown difficulty.
     */
    public Level(List<String> lines, List<String> authors, List<String> emails, String homepage,
            String copyright, String name, String info, int difficulty) {
        this(new Map(lines), authors, emails, homepage, copyright, name, info, difficulty);
        
        // compressed_map = CompressedMap(m_map);
        
        getInfo(lines);

        if (difficulty == -1) {
            setDifficulty(difficulty);
        }

    }

    /**
     * Returns the map of the level.
     */
    public Map getMap() {
        return map;
    }

    /**
     * Sets the map of the level.
     *
     * @param map The new map.
     */
    void setMap(Map map) {
        this.map = map;
    }

    /**
     * Returns the compressed map of the level.
     */
    // CompressedMap const & compressedMap() const;

    /**
     * Returns the authors of the level.
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * Returns the authors in one line, separated by ", ".
     */
    public String getAuthorLine() {
        StringBuilder result = new StringBuilder();

        int number_of_authors = authors.size();

        for (int i = 0; i < number_of_authors; ++i) {
            if (i != 0) {
                result.append(", ");
            }

            result.append(authors.get(i));
        }

        return result.toString();
    }

    /**
     * Returns the authors and emails in one line, separated by ", ".
     */
    public String getAuthorEmailLine() {
        return createAuthorEmailLine(authors, emails);
    }

    /**
     * Sets the authors of the level.
     *
     * @param authors The new author.
     */
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    /**
     * Sets the authors and emails created from the line.
     *
     * @param author_email_line The line containing (separated by ',') the authors and emails.
     */
    public void setAuthorEmailLine(String author_email_line) {
        parseAuthorEmailLine(author_email_line, authors, emails);
    }

    /**
     * Returns the email addresses of the authors.
     */
    public List<String> getEmails() {
        return emails;
    }

    /**
     * Sets the email addresses of the authors.
     *
     * @param emails The email addresses of the authors.
     */
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    /**
     * Returns the homepage of the level.
     */
    public String getHomepage() {
        return homepage;
    }

    /**
     * Sets the homepage of the level.
     *
     * @param homepage The new homepage.
     */
    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    /**
     * Returns the copyright the the level.
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * Sets the copyright of the level.
     *
     * @param copyright The new copyright.
     */
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    /**
     * Returns the name of the level.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the level.
     *
     * @param name The new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the additional info for this level.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Sets the info of the level.
     *
     * @param info The new info.
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Returns the difficulty of the level.
     */
    public int getDifficulty() {
        return difficulty;
    }

    /**
     * Sets the difficulty of the level.
     *
     * @param difficulty The new difficulty.
     */
    public void setDifficulty(int difficulty) {
        if ((difficulty >= 0) && (difficulty <= 10)) {
            this.difficulty = difficulty;
        }
        else {
            this.difficulty = -1;
        }
    }

    /**
     * Writes the level to a QDataStream.
     *
     * @param stream The stream to use.
     */
    // void writeToStream(QDataStream & stream) const;


    /**
     * Returns the map of the level collection plus additional info in xsb format.
     *
     * Note that only these informations are added, which differ from the ones in
     * the collection.
     *
     * @param authors The authors of the collection.
     * @param emails The email addresses of the authors of the collection.
     * @param homepage The homepage of the collection.
     * @param copyright The copyright of the collection.
     * @param info The info of the collection.
     * @param difficulty The difficulty of the collection.
     */

    public String toText(List<String> authors, List<String> emails, String homepage,
            String copyright, String info, int difficulty) {
        assert authors.size() == emails.size();

        String result = map.toString();

        if (((!this.authors.equals(authors)) && !this.authors.isEmpty()) || 
            ((!this.emails.equals(emails)) && !this.emails.isEmpty())) {
            result += "Author: " + getAuthorEmailLine() + '\n';
        }

        if ((!this.homepage.equals(homepage)) && !this.homepage.isEmpty()) {
            result += "Homepage: " + this.homepage + '\n';
        }

        if ((!this.copyright.equals(copyright)) && !this.copyright.isEmpty()) {
            result += "Copyright: " + this.copyright + '\n';
        }

        if (!this.name.isEmpty()) {
            result += "Name: " + this.name + '\n';
        }

        if ((!this.info.equals(info)) && !this.info.isEmpty()) {
            String[] parts = this.info.split("\n");
            
            for (String part: parts) {
                result += "Info: " + part + "\n";
            }
        }

        if (this.difficulty != difficulty) {
            result += "Difficulty: " + this.difficulty + '\n';
        }

        return result;
    }

    /**
     * Read author etc. information of a list of lines,  until a sokoban map is found.
     *
     * The constructor removes all lines in the string list, which were before the
     * actual map.
     *
     * Note that you have to supply default values for all author etc, which will only
     * be overwritten, if there exist other information.
     *
     * @param lines The list with the lines.
     * @param authors The authors the of the level.
     * @param emails The the email addresses of the authors the of the level.
     * @param homepage The homepage of the author.
     * @param copyright The copyright of the level.
     * @param name The name of the level.
     * @param difficulty The difficulty of the level.
     */

    private void getInfo(List<String> lines) {
        difficulty = -1;

        boolean had_info = !info.isEmpty();

        while (!lines.isEmpty() && !Map.isMapLine(lines.get(0)) && !lines.get(0).equals("+-+-")) {
            String act_line = lines.get(0);
            lines.remove(0);

            if (act_line.startsWith("Author:")) {
                parseAuthorEmailLine(act_line.substring(7), authors, emails);
            }
            else if (act_line.startsWith("Homepage:")) {
                homepage = act_line.substring(9).trim();
            }
            else if (act_line.startsWith("Copyright:")) {
                copyright = act_line.substring(10).trim();
            }
            else if (act_line.startsWith("Name:")) {
                name = act_line.substring(5).trim();
            }
            else if (act_line.startsWith("Title:")) {
                name = act_line.substring(6).trim();
            }
            else if (act_line.startsWith("Info:") || act_line.startsWith("comment:")) {
                if (had_info) {
                    had_info = false;
                    info = "";
                }

                info += act_line.substring(5).trim() + '\n';
            }
            else if (act_line.startsWith("Difficulty:")) {
                difficulty = Integer.parseInt(act_line.substring(11).trim());

                if ((difficulty < 0) || (difficulty > 10)) {
                    difficulty = -1;
                }
            }
        }
    }


    /**
     * Creates authors and emails from an author-email-line.
     *
     * @param author_email_line The author-email-line.
     * @param authors Here we store the authors.
     * @param email Here we store the email addresses.
     */
    public static void parseAuthorEmailLine(String author_email_line, List<String> authors, 
            List<String> emails) {

        authors.clear();
        emails.clear();

        String[] raw_authors = author_email_regexp.split(author_email_line);

        for (int i = 0; i < raw_authors.length; ++i) {
            String[] author_email = email_regexp.split(raw_authors[i]);

            authors.add(author_email[0].trim());

            if (author_email.length > 1) {
                emails.add(author_email[1].trim());
            }
            else {
                emails.add("");
            }
        }
    }


    /**
     * Creates the author-email-line.
     *
     * @param authors The authors.
     * @param emails The emails.
     */
    public static String createAuthorEmailLine(List<String> authors, List<String> emails) {
        assert authors.size() == emails.size();

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < authors.size(); ++i) {
            if (i != 0) {
                result.append(", ");
            }
            
            result.append(authors.get(i));

            if (!emails.get(i).isEmpty()) {
                result.append(" <");
                result.append(emails.get(i));
                result.append('>');
            }
        }

        return result.toString();
    }
    
    /**
     * Returns the level as a string.
     */
    public String toString() {
        return toText(authors, emails, homepage, copyright, info, difficulty);
    }
}
