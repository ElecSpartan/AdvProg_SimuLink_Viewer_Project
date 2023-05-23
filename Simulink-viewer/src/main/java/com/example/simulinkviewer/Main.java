package com.example.simulinkviewer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Main extends Application {
    static private List<Block> blocks = new ArrayList<Block>(); // for the blocks
    static private List<Arrow> connections = new ArrayList<Arrow>(); // for the connections
    static private Group root = new Group();
    
    @Override
    public void start(Stage stage) throws IOException, ParserConfigurationException, SAXException {
        Scene scene = new Scene(root, 1500, 790);  // setting the width and height of the window
        stage.setTitle("Simulink viewer");               // setting title of the window

        Image image = new Image("1.png");
        ImageView imageView = new ImageView(image);
        stage.getIcons().add(imageView.getImage());     // setting icon of the program
        mdlParsing();                                   // parsing the mdl file
        drawBlocks();
        drawArrows();


        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());   // setting the style of the window
        stage.setScene(scene);
        mouseEvents(scene, stage);
        stage.show();   // displaying
    }

    public static void mdlParsing() throws IOException, ParserConfigurationException, SAXException {

        File file = new File("untitled.mdl");
        FileInputStream input = new FileInputStream(file);
        StringBuilder mdlFile = new StringBuilder();
        int q;
        while ((q = input.read()) != -1) {
            mdlFile.append((char) q);
        }
        String mdlFileS = mdlFile.toString();
        Scanner scanner = new Scanner(mdlFileS);
        StringBuilder a = new StringBuilder();
        String before = "-1";
        boolean now = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("<System>")) {
                now = true;
            } else if (line.equals("</System>")) {
                a.append(before + '\n');
                a.append(line);
                break;
            }
            if (now)
                a.append(before + '\n');
            before = line;
        }
        String newMdlFile = a.toString();
        String outputFileName = "neededFile.mdl";
        FileOutputStream outputStream = new FileOutputStream(outputFileName);
        outputStream.write(newMdlFile.getBytes());


        file = new File("neededFile.mdl");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        Element rootElement = doc.getDocumentElement();
        doc.getDocumentElement().normalize();


        addBlocks(rootElement, doc);
        addArrows(rootElement, doc);
    }

public static void addBlocks(Element rootElement, Document doc) {
        if (rootElement.getTagName().equals("System")) {
            NodeList blockList = doc.getElementsByTagName("Block");
            for (int i = 0; i < blockList.getLength(); i++) {
                boolean inputs_ports_position_flag = false; // will be used to check if the block had a input ports number or not
                boolean blockMirror = false; // will be used to check if the block input and output will be mirrored or not
                Node blockNode = blockList.item(i);
                NodeList childNodes = blockNode.getChildNodes();
                if (blockNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element blockElement = (Element) blockNode;

                    //Extracting the information from the block tag
                    String Name = blockElement.getAttribute("Name");
                    String BlockType = blockElement.getAttribute("BlockType");
                    int ID = (Integer.parseInt(blockElement.getAttribute("SID")));

                    //this part parse the position string to extract the 4 coordinates of the block and handles weather it's on index 0 or index 1
                    double value1 = 0, value2 = 2, value3 = 0, value4 = 0;
                    for (int j = 0; j < blockElement.getElementsByTagName("P").getLength(); j++) {
                        if (blockElement.getElementsByTagName("P").item(j).getAttributes().item(0).getTextContent().equals("Position")) {
                            String Position = blockElement.getElementsByTagName("P").item(j).getTextContent();
                            Position = Position.replace("[", "").replace("]", ""); // Remove square brackets
                            String[] strValues = Position.split(","); // Split by comma
                            value1 = Double.parseDouble(strValues[0]);
                            value2 = Double.parseDouble(strValues[1]);
                            value3 = Double.parseDouble(strValues[2]);
                            value4 = Double.parseDouble(strValues[3]);
                        }
                    }


                    // this part extract the number of input and output ports and the flag is used to
                    int NumInputPorts = 1, NumOutputPorts = 1;

                        String ports = blockElement.getElementsByTagName("P").item(0).getTextContent();
                        if (ports.length() == 6) {
                            NumInputPorts = ports.charAt(1) - '0';
                            NumOutputPorts = ports.charAt(4) - '0';
                        } else if (ports.length() == 3) {
                            NumInputPorts = ports.charAt(1) - '0';
                            NumOutputPorts = 0;
                        }


                    // extracting the inputs if the BlockType is ADD
                     String inputs = ""; // for Add class ( signs )
                    if(BlockType.equals("Sum")){
                            for (int j = 0; j < blockElement.getElementsByTagName("P").getLength(); j++) {
                                if (blockElement.getElementsByTagName("P").item(j).getAttributes().item(0).getTextContent().equals("Inputs")) {
                                    inputs = blockElement.getElementsByTagName("P").item(j).getTextContent();
                                    break;
                                }
                            }
                        }

                    for (int j = 0; j < blockElement.getElementsByTagName("P").getLength(); j++) {
                        if (blockElement.getElementsByTagName("P").item(j).getAttributes().item(0).getTextContent().equals("BlockMirror")) {
                            if (blockElement.getElementsByTagName("P").item(j).getTextContent().equals("on"))
                                blockMirror = true;
                        }
                    }

                    String value = "1"; // for constant class ( value )

                    Block b = switch (BlockType) {
                        case "Saturate" ->
                                new Saturation(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror);
                        case "UnitDelay" ->
                                new UnitDelay(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror);
                        case "Scope" ->
                                new Scope(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror);
                        case "Sum" ->
                                new Add(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror, inputs);
                        case "Constant" ->
                                new Constant(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror, value);
                        default ->
                                new Block(ID, Name, value1, value2, value3, value4, NumInputPorts, NumOutputPorts, blockMirror);
                    };

                    blocks.add(b);
                }
            }
        }
    }
    public static void addArrows(Element rootElement, Document doc) {
        if(rootElement.getTagName().equals("System")){
            NodeList lineList = doc.getElementsByTagName("Line");
            for (int i = 0; i < lineList.getLength(); i++) {
                Node lineNode = lineList.item(i);
                //NodeList childNodes = lineNode.getChildNodes();
                if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element lineElement = (Element) lineNode;
                    NodeList branchList = lineElement.getElementsByTagName("Branch");
                    // looping on the tags to find srcID srcPlace
                    int srcId=0,srcPlace=0;
                    for (int j = 0; j < lineElement.getElementsByTagName("P").getLength(); j++) {
                        if(lineElement.getElementsByTagName("P").item(j).getAttributes().item(0).getTextContent().equals("Src")){
                            String src=(lineElement.getElementsByTagName("P").item(j).getTextContent());
                            String srcIdInfo=""; String srcPlaceInfo="";
                            for(int k=0;k<src.length();k++){
                                if(src.charAt(k)=='#')
                                    break;
                                else
                                    srcIdInfo+=src.charAt(k);
                            }
                            int idx=-1;
                            for(int k=0;k<src.length();k++){
                                if(src.charAt(k)==':') {
                                    idx=k+1;
                                    break;
                                }
                            }
                            for(int k=idx;k<src.length();k++)
                                srcPlaceInfo+=src.charAt(k);
                            srcId = Integer.parseInt(srcIdInfo);
                            srcPlace = Integer.parseInt(srcPlaceInfo);
                            //System.out.println(srcId+" "+srcPlace);
                        }
                    }

                    //getting x and y of the line if there is no branches
                    double x=0,y=0;
                    String points="";
                    if(getPIndexByName(lineElement, "Points")!=-1)
                        points = lineElement.getElementsByTagName("P").item(getPIndexByName(lineElement, "Points")).getTextContent();
                    if(points.length()>0) {
                        String pointInfo="";
                        for(int k=1;k<points.length();k++){
                            if(points.charAt(k)==',')
                                break;
                            else
                                pointInfo+=points.charAt(k);
                        }
                        x=Double.parseDouble(pointInfo);
                        //to check if the string has y coordinations
                        boolean yFlag=false;
                        for(int k=0;k<points.length();k++){
                            if(points.charAt(k)==';') yFlag=true;
                        }
                        if(yFlag){
                            int index=-1;
                            String yString = "";
                            for(int k=points.length()-2;k>=0;k--){
                                if(points.charAt(k)==' ') {
                                    index=k+1;
                                    break;
                                }
                            }
                            for(int k=index;k<points.length()-1;k++)  yString+=points.charAt(k);
                            y=Double.parseDouble(yString);
                        }
                    }

                    /////////////////the object//////////////////////////
                    Arrow arrow = new Arrow(srcId,srcPlace,x);


                    // dstID if there is no branches
                    if(branchList.getLength()==0){
                        int destId=0 ;
                        for (int j = 0; j < lineElement.getElementsByTagName("P").getLength(); j++) {
                            if(lineElement.getElementsByTagName("P").item(j).getAttributes().item(0).getTextContent().equals("Dst")){
                                String dst=(lineElement.getElementsByTagName("P").item(j).getTextContent());
                                String dstIdInfo="";
                                for(int k=0;k<dst.length();k++){
                                    if(dst.charAt(k)=='#')
                                        break;
                                    else
                                        dstIdInfo+=dst.charAt(k);
                                }
                                destId = Integer.parseInt(dstIdInfo);
                                //System.out.println(destId);
                            }
                        }
                        arrow.addDest(destId,y);
                        connections.add(arrow);
                    }

                    //in case of branches
                    else{
                        //System.out.println(branchList.getLength());
                        for (int j = 0; j < branchList.getLength(); j++) {
                            Node branchNode = branchList.item(j);
                            if (branchNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element branchElement = (Element) branchNode;
                                int destIdBranch=0;
                                double yBranch=0;
                                for (int k = 0; k < branchElement.getElementsByTagName("P").getLength(); k++) {
                                    if (branchElement.getElementsByTagName("P").item(k).getAttributes().item(0).getTextContent().equals("Dst")) {
                                        String dstBranch=(branchElement.getElementsByTagName("P").item(k).getTextContent());
                                        String dstIdInfo="";
                                        for(int f=0;f<dstBranch.length();f++){
                                            if(dstBranch.charAt(f)=='#')
                                                break;
                                            else
                                                dstIdInfo+=dstBranch.charAt(f);
                                        }
                                        destIdBranch = Integer.parseInt(dstIdInfo);
                                        //System.out.println(destIdBranch+" "+yBranch);
                                        arrow.addDest(destIdBranch,yBranch);
                                        connections.add(arrow);
                                    }
                                    else if(branchElement.getElementsByTagName("P").item(k).getAttributes().item(0).getTextContent().equals("Points")){
                                        String pointsBranch=(branchElement.getElementsByTagName("P").item(k).getTextContent());
                                        String yBranchString="";
                                        int idx2=-1;
                                        for(int f=pointsBranch.length()-2;f>=0;f--){
                                            if(pointsBranch.charAt(f)==' ') idx2=f+1;
                                        }
                                        for(int f=idx2;f<pointsBranch.length()-1;f++)
                                            yBranchString+=pointsBranch.charAt(f);
                                        yBranch=Double.parseDouble(yBranchString);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private static int getPIndexByName(Element element, String name) {
        NodeList pList = element.getElementsByTagName("P");
        for (int i = 0; i < pList.getLength(); i++) {
            Element pElement = (Element) pList.item(i);
            if (pElement.getAttribute("Name").equals(name)) {
                return i;
            }
        }
        return -1; // Not found
    }
    
      public static void drawBlocks() {
        for (Block b : blocks) {
            b.addBlock(root);
        }
    }
    public static void drawArrows() {
        double arrowSize = 5;
        for (Arrow a : connections) {
            Block b;
            b = getBlock(a.getScrId());
            double left = b.getLeft();
            double width = b.getWidth();
            double startX = left;
            if (!b.isMirror())
                startX = left + width;
            double div = b.getHeight() / (b.getOutputsNum() + 1);
            double startY = b.getUp() + a.getSrcPlace() * div;
            Line l = new Line();
            l.setStartX(startX);
            l.setStartY(startY);
            double endX = startX + a.getX();
            double endY = startY;
            l.setEndX(endX);
            l.setEndY(endY);
            root.getChildren().add(l);
            startX = endX;
            startY = endY;
            double sX = startX;
            double sY = startY;
            if (a.getDestsSize() > 1) {
                Circle c = new Circle(startX, startY, 3);
                root.getChildren().add(c);
            }

            for (int i = 0; i < a.getDestsSize(); i++) {
                endX = startX;
                endY = startY + a.getY(i);
                Line l2 = new Line(startX, startY, endX, endY);
                root.getChildren().add(l2);
                startX = endX;
                startY = endY;
                endY = startY;
                b = getBlock(a.getDestId(i));
                endX = b.getLeft();
                if (b.isMirror())
                    endX += b.getWidth();

                Line l3 = new Line(startX, startY, endX, endY);
                root.getChildren().add(l3);

                if (!b.isMirror())
                    root.getChildren().add(new Polygon(endX, endY, endX - arrowSize, endY + arrowSize, endX - arrowSize, endY - arrowSize));
                else
                    root.getChildren().add(new Polygon(endX, endY, endX + arrowSize, endY + arrowSize, endX + arrowSize, endY - arrowSize));

                startX = sX;
                startY = sY;
            }

        }
    }

    public static Block getBlock(int id) {
        for (Block b : blocks) {
            if (b.getID() == id)
                return b;
        }
        return null;
    }

    public static void mouseEvents(Scene scene, Stage stage) {
        scene.setOnScroll(e -> {
            double zoomFactor = 1.05;
            double deltaY = e.getDeltaY();

            if (deltaY < 0) {
                zoomFactor = 0.95;
            }
            root.setScaleX(root.getScaleX() * zoomFactor);
            root.setScaleY(root.getScaleY() * zoomFactor);
        });

    }
}
