import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class QuizManagementSystem extends JFrame {
    static final String DATA = "quiz-data.ser";
    static Store store = load();
    CardLayout cards = new CardLayout();
    JPanel root = new JPanel(cards);
    JTextField studentName = new JTextField(18), rollNo = new JTextField(18);

    static class Question implements Serializable {
        String text; String[] options; int answer;
        Question(String t, String[] o, int a){ text=t; options=o; answer=a; }
    }
    static class Quiz implements Serializable {
        String title; List<Question> questions = new ArrayList<>();
        Quiz(String t){title=t;}
    }
    static class Result implements Serializable {
        String name, roll, quiz; int score, total; String date;
        Result(String n,String r,String q,int s,int t){
            name=n; roll=r; quiz=q; score=s; total=t;
            date=new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
        }
    }
    static class Store implements Serializable {
        List<Quiz> quizzes = new ArrayList<>();
        List<Result> results = new ArrayList<>();
        Map<String,Integer> attempts = new HashMap<>();
    }
    static Store load(){
        try(ObjectInputStream in=new ObjectInputStream(new FileInputStream(DATA))){
            return (Store)in.readObject();
        } catch(Exception e){
            Store s=new Store();
            Quiz q=new Quiz("Java Basics");
            q.questions.add(new Question("Which keyword is used to inherit a class in Java?",
                new String[]{"implements","extends","inherits","super"},1));
            q.questions.add(new Question("Which method is the entry point of a Java application?",
                new String[]{"start()","run()","main()","init()"},2));
            q.questions.add(new Question("Which type stores true or false?",
                new String[]{"int","String","boolean","char"},2));
            s.quizzes.add(q); return s;
        }
    }
    static void save(){
        try(ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(DATA))){
            out.writeObject(store);
        } catch(IOException e){ JOptionPane.showMessageDialog(null,"Could not save data: "+e.getMessage()); }
    }

    QuizManagementSystem(){
        setTitle("Online Quiz Management System");
        setSize(850,580); setMinimumSize(new Dimension(720,500));
        setDefaultCloseOperation(EXIT_ON_CLOSE); setLocationRelativeTo(null);
        root.add(home(),"home"); root.add(teacher(),"teacher"); root.add(student(),"student");
        add(root); cards.show(root,"home");
    }
    JPanel page(String title){
        JPanel p=new JPanel(new BorderLayout(15,15)); p.setBorder(BorderFactory.createEmptyBorder(24,30,24,30));
        JLabel h=new JLabel(title,SwingConstants.CENTER); h.setFont(new Font("Arial",Font.BOLD,26));
        p.add(h,BorderLayout.NORTH); return p;
    }
    JButton button(String text){ JButton b=new JButton(text); b.setFont(new Font("Arial",Font.BOLD,15)); return b; }
    JPanel home(){
        JPanel p=page("ONLINE QUIZ MANAGEMENT SYSTEM");
        JLabel sub=new JLabel("Java Hackathon Project  |  Learn • Attempt • Score",SwingConstants.CENTER);
        sub.setFont(new Font("Arial",Font.PLAIN,16));
        JPanel center=new JPanel(new GridLayout(4,1,12,12));
        center.add(sub); JButton t=button("TEACHER / ADMIN"); JButton s=button("STUDENT"); JButton exit=button("EXIT");
        t.addActionListener(e->{refreshTeacher();cards.show(root,"teacher");});
        s.addActionListener(e->{cards.show(root,"student");});
        exit.addActionListener(e->System.exit(0));
        center.add(t);center.add(s);center.add(exit);p.add(center,BorderLayout.CENTER);return p;
    }
    JPanel teacher(){
        JPanel p=page("TEACHER DASHBOARD");
        JTextArea info=new JTextArea(); info.setEditable(false); info.setFont(new Font("Monospaced",0,14));
        JButton add=button("Create New Quiz"), records=button("View Score Records"), back=button("Back");
        add.addActionListener(e->createQuiz());
        records.addActionListener(e->showRecords());
        back.addActionListener(e->cards.show(root,"home"));
        JPanel south=new JPanel();south.add(add);south.add(records);south.add(back);
        p.add(new JScrollPane(info),BorderLayout.CENTER);p.add(south,BorderLayout.SOUTH);
        p.putClientProperty("info",info); return p;
    }
    void refreshTeacher(){
        JPanel p=(JPanel)root.getComponent(1); JTextArea area=(JTextArea)p.getClientProperty("info");
        StringBuilder b=new StringBuilder("AVAILABLE QUIZZES\\n------------------------------\\n");
        for(Quiz q:store.quizzes)b.append("• ").append(q.title).append(" (").append(q.questions.size()).append(" questions)\\n");
        area.setText(b.toString());
    }
    void createQuiz(){
        String title=JOptionPane.showInputDialog(this,"Enter quiz title:");
        if(title==null||title.trim().isEmpty())return;
        Quiz q=new Quiz(title.trim());
        while(true){
            JTextField question=new JTextField(), a=new JTextField(), b=new JTextField(), c=new JTextField(), d=new JTextField();
            JComboBox<String> correct=new JComboBox<>(new String[]{"A","B","C","D"});
            JPanel form=new JPanel(new GridLayout(0,2,6,6));
            form.add(new JLabel("Question"));form.add(question);
            JTextField[] fields={a,b,c,d}; String[] labels={"Option A","Option B","Option C","Option D"};
            for(int i=0;i<4;i++){form.add(new JLabel(labels[i]));form.add(fields[i]);}
            form.add(new JLabel("Correct option"));form.add(correct);
            int choice=JOptionPane.showConfirmDialog(this,form,"Add question",JOptionPane.OK_CANCEL_OPTION);
            if(choice!=JOptionPane.OK_OPTION)break;
            boolean valid=!question.getText().trim().isEmpty();
            for(JTextField f:fields)valid &= !f.getText().trim().isEmpty();
            if(!valid){JOptionPane.showMessageDialog(this,"Please fill every field.");continue;}
            q.questions.add(new Question(question.getText().trim(),
                new String[]{a.getText().trim(),b.getText().trim(),c.getText().trim(),d.getText().trim()},correct.getSelectedIndex()));
            int more=JOptionPane.showConfirmDialog(this,"Add another question?","Continue",JOptionPane.YES_NO_OPTION);
            if(more!=JOptionPane.YES_OPTION)break;
        }
        if(q.questions.isEmpty()){JOptionPane.showMessageDialog(this,"Quiz not saved: add at least one question.");return;}
        store.quizzes.add(q);save();refreshTeacher();
        JOptionPane.showMessageDialog(this,"Quiz created successfully!");
    }
    JPanel student(){
        JPanel p=page("STUDENT PORTAL");
        JPanel form=new JPanel(new GridLayout(3,2,10,10));
        form.add(new JLabel("Student name"));form.add(studentName);
        form.add(new JLabel("Roll number"));form.add(rollNo);
        form.add(new JLabel("Select quiz"));
        JComboBox<String> quizzes=new JComboBox<>(); for(Quiz q:store.quizzes)quizzes.addItem(q.title);
        form.add(quizzes);
        JButton start=button("Start Quiz"), back=button("Back");
        start.addActionListener(e->{
            if(studentName.getText().trim().isEmpty()||rollNo.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(this,"Enter student name and roll number.");return;
            }
            if(quizzes.getSelectedIndex()<0){JOptionPane.showMessageDialog(this,"No quizzes available.");return;}
            Quiz q=store.quizzes.get(quizzes.getSelectedIndex());
            String key=rollNo.getText().trim()+"|"+q.title;
            int used=store.attempts.getOrDefault(key,0);
            if(used>=3){JOptionPane.showMessageDialog(this,"Attempt limit reached (3 attempts for this quiz).");return;}
            attempt(q,key);
        });
        back.addActionListener(e->cards.show(root,"home"));
        JPanel center=new JPanel(new BorderLayout(15,15));center.add(new JLabel("Enter your details and choose a quiz.",SwingConstants.CENTER),BorderLayout.NORTH);center.add(form,BorderLayout.CENTER);
        JPanel south=new JPanel();south.add(start);south.add(back);
        p.add(center,BorderLayout.CENTER);p.add(south,BorderLayout.SOUTH);return p;
    }
    void attempt(Quiz q,String key){
        int score=0;
        for(int i=0;i<q.questions.size();i++){
            Question x=q.questions.get(i);
            int ans=JOptionPane.showOptionDialog(this,
                (i+1)+". "+x.text,"Quiz: "+q.title,JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,null,x.options,x.options[0]);
            if(ans<0)return;
            if(ans==x.answer)score++;
        }
        store.attempts.put(key,store.attempts.getOrDefault(key,0)+1);
        Result r=new Result(studentName.getText().trim(),rollNo.getText().trim(),q.title,score,q.questions.size());
        store.results.add(r);save();
        JOptionPane.showMessageDialog(this,"Quiz submitted!\\nStudent: "+r.name+"\\nScore: "+score+"/"+q.questions.size()+"\\nPercentage: "+(score*100/q.questions.size())+"%");
    }
    void showRecords(){
        String[] cols={"Student","Roll No.","Quiz","Score","Date"};
        DefaultTableModel model=new DefaultTableModel(cols,0);
        for(Result r:store.results)model.addRow(new Object[]{r.name,r.roll,r.quiz,r.score+"/"+r.total,r.date});
        JTable table=new JTable(model);table.setRowHeight(24);
        JOptionPane.showMessageDialog(this,new JScrollPane(table),"Quiz Score Records",JOptionPane.PLAIN_MESSAGE);
    }
    public static void main(String[] args){SwingUtilities.invokeLater(()->new QuizManagementSystem().setVisible(true));}
}
