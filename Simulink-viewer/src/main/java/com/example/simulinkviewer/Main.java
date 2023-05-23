package com.example.simulinkviewer;

public class Main extends Application {
    static private List<Block> blocks = new ArrayList<Block>(); // for the blocks
    static private List<Arrow> connections = new ArrayList<Arrow>(); // for the connections


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
}
