/*
 * Copyright (c) 2009,2014 Serhiy Kulyk
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *      1. Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      2. Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *
 * SQL CODE ASSISTANT PLUG-IN FOR INTELLIJ IDEA IS PROVIDED BY SERHIY KULYK
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SERHIY KULYK BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.deepsky.lang.plsql.completion.syntaxTreePath.generator;

import com.deepsky.lang.common.PlSqlTokenTypes;
import com.deepsky.lang.parser.plsql.PlSqlElementTypes;
import com.deepsky.lang.plsql.completion.syntaxTreePath.structures.*;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.*;


public class CodeGeneratorVisitor implements TNodeVisitor {

    WriterAdapter writer = null;
    Stack<String> methodStack = new Stack<String>();


    public CodeGeneratorVisitor(){
        this.writer = new WriterAdapter(){
            @Override
            public void println(String x) {
                System.out.println(x);
            }

            @Override
            public void println() {
                System.out.println();
            }

            @Override
            public void print(String x) {
                System.out.print(x);
            }
        };

    }

    public CodeGeneratorVisitor(final Writer writer){
        this.writer = new WriterAdapter() {
            @Override
            public void println(String x) {
                try {
                    writer.write(x + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void println() {
                try {
                    writer.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void print(String x) {
                try {
                    writer.write(x);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void visitRoot(RootNode root) {

        writer.println("package com.deepsky.lang.plsql.completion.syntaxTreePath.generated;");
        writer.println();
        writer.println("// AutoGenerated class");
        writer.println("import com.deepsky.lang.plsql.completion.syntaxTreePath.generator.*;");
        writer.println("import com.deepsky.lang.plsql.completion.syntaxTreePath.logic.TreePath;");
        writer.println("import com.deepsky.lang.parser.plsql.PlSqlElementTypes;");
        writer.println("import com.intellij.lang.ASTNode;");
        writer.println("import com.deepsky.lang.plsql.psi.*;");
        writer.println("import com.deepsky.lang.common.PlSqlTokenTypes;");
        writer.println();
        writer.println("public class " + root.getProcessorClassName() + " extends CompletionProcessorBase {");
        writer.println();

        int pathListSize = root.getTreePathListSize();
        // Generate Path List
        writer.println("\tstatic final String[] pathList = new String[]{");
        for(int i =0; i<pathListSize; i++){
            writer.println("\t\t\"" + root.getTreePath(i) + "\",");
        }
        writer.println("\t};");
        writer.println();

        // Generate Argument index List
        writer.println("\tstatic final int[][] parameterIndexList = new int[][]{");
        for(int i =0; i<pathListSize; i++){
            int[] indices = root.getParameterIndexList(i);
            writer.print("\t\t{");
            for(int j =0; j<indices.length; j++){
                writer.print(indices[j] + ",");
            }
            writer.println("},");
        }
        writer.println("\t};");
        writer.println();

        // Generate Argument Class List
        writer.println("\t// A parameter list of the completion method handler");
        writer.println("\tstatic final Class[][] parameterClassList = new Class[][]{");
        for(int i =0; i<pathListSize; i++){
            writer.print("\t\t{");
            for(String clazz: root.getParameterClassList(i)) {
                writer.print(clazz + ".class");
                writer.print(",");
            }
            writer.println("},");
        }
        writer.println("\t};");
        writer.println();

        // Generate completion method handler List
        writer.println("\t// Completion method handler List");
        writer.println("\tstatic final String[][] classPlusMethodList = new String[][]{");
        for(int i =0; i<pathListSize; i++){
            writer.println("\t\t{\"" + root.getClassFor(i) +"\", \"" + root.getMethodFor(i) + "\"},");
        }
        writer.println("\t};");
        writer.println();

        writer.println("\tprivate TreePathContext context;");
        writer.println();
        writer.println("\tpublic " + root.getProcessorClassName() + "(TreePath treePath) {");
        writer.println("\t\tsuper(treePath);");
        writer.println("\t}");
        writer.println();

        // Generate accessor methods -------------------------
        writer.println("\tprotected String getTreePath(int index) {");
        writer.println("\t\treturn pathList[index];");
        writer.println("\t}");
        writer.println();

        writer.println("\tprotected String[] getClassPlusMethod(int index) {");
        writer.println("\t\treturn classPlusMethodList[index];");
        writer.println("\t}");
        writer.println();

        writer.println("\tprotected Class[] getMethodParamClasses(int index) {");
        writer.println("\t\treturn parameterClassList[index];");
        writer.println("\t}");
        writer.println();

        writer.println("\tprotected int[] getMethodParamIndexes(int index) {");
        writer.println("\t\treturn parameterIndexList[index];");
        writer.println("\t}");
        writer.println();

        writer.println("\tpublic TreePathContext getContext() {");
        writer.println("\t\treturn context;");
        writer.println("\t}");
        writer.println();
        // --------------------------------------------------------

        generateMethodComment(root);

        writer.println("public boolean process() throws InvalidLexerStateException, ClassCastException {");
        writer.println("\tcontext = new TreePathContextImpl();");
        writer.println("\ttry {");
        writer.println("\t\tObject o = peek();");
        writer.println("\t\tif (!(o instanceof SlashNode))");
        writer.println("\t\t\treturn false;");
        writer.println();
        writer.println("\t\tint lexerState = saveState();");

        String prefix = "\t\t";
        Map<TNode, String> node2MethodName = new HashMap<TNode, String>();

        TNode sSlash = find("/", root.getChildren());
        if (sSlash != null) {
            writer.println(prefix + "if (processSS(context)){");
            writer.println(prefix + "\treturn true;");
            writer.println(prefix + "}");
            writer.println(prefix + "setState(lexerState);");
            writer.println();

            node2MethodName.put(sSlash, "processSS");
        }

        TNode dSlash = find("//", root.getChildren());
        if (dSlash != null) {
            writer.println(prefix + "if (processDS(context)){");
            writer.println(prefix + "\treturn true;");
            writer.println(prefix + "}");
            writer.println();

            node2MethodName.put(dSlash, "processDS");
        }

        writer.println("\t} catch (EOFException ignored) {");
        writer.println("\t}");
        writer.println("\treturn false;");
        writer.println("}");
        writer.println();

        for (Map.Entry<TNode, String> e : node2MethodName.entrySet()) {
            methodStack.push(e.getValue());
            e.getKey().accept(this);
            methodStack.pop();
        }

        writer.println("}");
    }

    private TNode find(String name, List<TNode> children) {
        for (TNode n : children) {
            if (n.getName().equals(name)) {
                return n;
            }
        }
        return null;
    }

    @Override
    public void visitDD(DoubleDotNode node) {

        generateMethodComment(node);

        String nodeMethodName = methodStack.peek();
        writer.println("private boolean " + nodeMethodName + "(TreePathContext context) {");
        writer.println("\tTreePathContext.Marker m = context.createMarker(\"..\");");
        writer.println("\ttry {");
        writer.println("\t\twhile(true){");
        writer.println("\t\t\tObject o = peek();");
        writer.println("\t\t\tif (o instanceof SlashNode)");
        writer.println("\t\t\t\tbreak;");
        writer.println();
        writer.println("\t\t\tASTNode n = (ASTNode) o;");

        Map<TNode, String> node2MethodName = new HashMap<TNode, String>();
        for (TNode n : node.getChildren()) {
            generateDD_Call(n, "\t\t\t", nodeMethodName, node2MethodName);
        }

        writer.println("\t\t\tconsume(ASTNode.class);");
        writer.println("\t\t}");
        writer.println("\t} catch (EOFException ignored) {");
        writer.println("\t}");
        writer.println("\tm.discard();");
        writer.println("\treturn false;");
        writer.println("}");
        writer.println();

        for (Map.Entry<TNode, String> e : node2MethodName.entrySet()) {
            methodStack.push(e.getValue());
            e.getKey().accept(this);
            methodStack.pop();
        }
    }

    private String generateDD_Call(TNode node, final String prefix, final String parentMethodName, final Map<TNode, String> node2MethodName) {
        String methodName = null;
        if (node instanceof StringNode) {
            writer.println(prefix + "if (" + generateConditionForSS(true, (StringNode) node) + ") {");
            writer.println(prefix + "\tint lexerState = saveState();");
            methodName = parentMethodName + "_" + node.getName();
            writer.println(prefix + "\tif (" + methodName + "(context)){");
            writer.println(prefix + "\t\treturn true;");
            writer.println(prefix + "\t}");
            writer.println(prefix + "\tsetState(lexerState);");
            writer.println(prefix + "}");

            node2MethodName.put(node, methodName);
        }

        return methodName;
    }

    @Override
    public void visitDS(DoubleSlashNode node) {
        generateMethodComment(node);

        String nodeMethodName = methodStack.peek();
        writer.println("private boolean " + nodeMethodName + "(TreePathContext context) {");
        writer.println("\tTreePathContext.Marker m = context.createMarker(\"//\");");
        writer.println("\ttry {");
        writer.println("\t\twhile(true){");
        writer.println("\t\t\tObject o = next();");
        writer.println("\t\t\tif (o instanceof SlashNode){");
        writer.println("\t\t\t\tASTNode n = peek(ASTNode.class);");

        Map<TNode, String> node2MethodName = new HashMap<TNode, String>();
        for (TNode n : node.getChildren()) {
            generateDS_Call(n, "\t\t\t\t", nodeMethodName, node2MethodName);
        }

        writer.println("\t\t\t}");
        writer.println("\t\t}");
        writer.println("\t} catch (EOFException ignored) {");
        writer.println("\t}");
        writer.println("\tm.discard();");
        writer.println("\treturn false;");
        writer.println("}");
        writer.println();

        for (Map.Entry<TNode, String> e : node2MethodName.entrySet()) {
            methodStack.push(e.getValue());
            e.getKey().accept(this);
            methodStack.pop();
        }
    }


    private void generateDS_Call(TNode node, String prefix, String parentMethodName, Map<TNode, String> node2MethodName) {
        String methodName = null;
        if (node instanceof DoubleDotNode) {
            writer.println(prefix + "{");
            writer.println(prefix + "\tint lexerState = saveState();");
            methodName = parentMethodName + "_DD";
            writer.println(prefix + "\tif (" + methodName + "(context)){");
            writer.println(prefix + "\t\treturn true;");
            writer.println(prefix + "\t}");
            writer.println(prefix + "\tsetState(lexerState);");
            writer.println(prefix + "}");

            node2MethodName.put(node, methodName);
        } else if (node instanceof StringNode) {
            // String node
            if (node.getChildren().size() == 0) {
                writer.println(prefix + "if (" + generateConditionForSS(true, (StringNode) node) + ") {");
                writer.println(prefix + "\tASTNode n1 = next(ASTNode.class);");
                writer.println(prefix + "\tTreePathContext.Marker m1 = context.createMarker(\"" + buildMarkerName((StringNode) node) + "\");");
                writer.println(prefix + "\tm1.setASTNode(n1, "+ ((StringNode) node).isDollar() + ");");
                writer.println(prefix + "\t// TODO - implement hit");
                writer.println(prefix + "\tcontext.setMetaInfoRef(" + ((StringNode) node).getMetaInfoRef() + ");");
                writer.println(prefix + "\treturn true;");
                writer.println(prefix + "}");
            } else {
                writer.println(prefix + "if (" + generateConditionForSS(true, (StringNode) node) + ") {");
                writer.println(prefix + "\tint lexerState = saveState();");
                methodName = parentMethodName + "_" + node.getName();
                writer.println(prefix + "\tif (" + methodName + "(context)){");
                writer.println(prefix + "\t\treturn true;");
                writer.println(prefix + "\t}");
                writer.println(prefix + "\tsetState(lexerState);");
                writer.println(prefix + "}");

                node2MethodName.put(node, methodName);
            }
        } else {
            System.err.println("No correct token 1!");
        }
    }


    @Override
    public void visitSS(SingleSlashNode node) {

        generateMethodComment(node);

        String nodeMethodName = methodStack.peek();
        writer.println("private boolean " + nodeMethodName + "(TreePathContext context) {");
        writer.println("\tTreePathContext.Marker m = context.createMarker(\"/\");");
        writer.println("\tint lexerState;");
        writer.println("\ttry {");
        writer.println("\t\tconsume(SlashNode.class);");
        writer.println("\t\tASTNode n = peek(ASTNode.class);");

        Map<TNode, String> node2MethodName = new HashMap<TNode, String>();
        for (TNode n : node.getChildren()) {
            String methodName = generateSS_Call(n, "\t\t", nodeMethodName);
            node2MethodName.put(n, methodName);
        }

        writer.println("\t} catch (EOFException ignored) {");
        writer.println("\t}");
        writer.println("\tm.discard();");
        writer.println("\treturn false;");
        writer.println("}");
        writer.println();

        for (TNode n : node.getChildren()) {
            String methodName = node2MethodName.get(n);
            if (methodName != null) {
                methodStack.push(methodName);
                n.accept(this);
                methodStack.pop();
            }
        }
    }


    private String generateSS_Call(TNode node, String prefix, String parentMethodName) {
        String methodName = null;
        if (node instanceof DoubleDotNode) {
            writer.println(prefix + "lexerState = saveState();");
            methodName = parentMethodName + "_DD";
            writer.println(prefix + "if (" + methodName + "(context)){");
            writer.println(prefix + "\treturn true;");
            writer.println(prefix + "}");
            writer.println(prefix + "setState(lexerState);");
        } else if (node instanceof StringNode) {
            // String node
            if (node.getChildren().size() == 0) {
                writer.println(prefix + "if (" + generateConditionForSS(true, (StringNode) node) + ") {");
                writer.println(prefix + "\tASTNode n1 = next(ASTNode.class);");
                writer.println(prefix + "\tTreePathContext.Marker m1 = context.createMarker(\"" + buildMarkerName((StringNode) node) + "\");");
                writer.println(prefix + "\tm1.setASTNode(n1, "+ ((StringNode) node).isDollar() + ");");
                writer.println(prefix + "\t// TODO - implement hit");
                writer.println(prefix + "\tcontext.setMetaInfoRef(" + ((StringNode) node).getMetaInfoRef() + ");");
                writer.println(prefix + "\treturn true;");
                writer.println(prefix + "}");
            } else {
                writer.println(prefix + "if (" + generateConditionForSS(true, (StringNode) node) + ") {");
                writer.println(prefix + "\tlexerState = saveState();");
                methodName = parentMethodName + "_" + node.getName();
                writer.println(prefix + "\tif (" + methodName + "(context)){");
                writer.println(prefix + "\t\treturn true;");
                writer.println(prefix + "\t}");
                writer.println(prefix + "\tsetState(lexerState);");
                writer.println(prefix + "}");
            }
        } else {
            System.err.println("No correct token 2!");
        }
        return methodName;
    }

    @Override
    public void visitStringNode(StringNode node) {

        generateMethodComment(node);

        String nodeMethodName = methodStack.peek();
        writer.println("private boolean " + nodeMethodName + "(TreePathContext context) {");
        writer.println("\tTreePathContext.Marker m = context.createMarker(\""+ buildMarkerName(node) + "\");");
        writer.println("\tint lexerState;");
        writer.println("\tObject o;");
        writer.println("\ttry {");
        writer.println("\t\tm.setASTNode(next(ASTNode.class), "+ node.isDollar() + ");  // Consume " + node.getName());

        Map<TNode, String> node2MethodName = new HashMap<TNode, String>();
        if(node.getChildren().size() == 0){
//            writer.println("\t\tTreePathContext.Marker m1 = context.createMarker(\"" + buildMarkerName(node) + "\");");
//            writer.println("\t\tm1.setASTNode((ASTNode)o, " + node.isDollar() + ");");
            writer.println("\t\tcontext.setMetaInfoRef(" + node.getMetaInfoRef() + ");");
            writer.println("\t\t// TODO - implement hit");
            writer.println("\t\treturn true;");
        } else {
            sortChildren(node);
            for (TNode n : node.getChildren()) {
                generateString_Call(n, "\t\t", nodeMethodName, node2MethodName);
            }
        }

        writer.println("\t} catch (EOFException ignored) {");
        writer.println("\t}");
        writer.println("\tm.discard();");
        writer.println("\treturn false;");
        writer.println("}");
        writer.println();

        for (Map.Entry<TNode, String> e : node2MethodName.entrySet()) {
            methodStack.push(e.getValue());
            e.getKey().accept(this);
            methodStack.pop();
        }
    }

    void sortChildren(StringNode node){
        Collections.sort(node.getChildren(), new Comparator<TNode>() {
            @Override
            public int compare(TNode o1, TNode o2) {
                int o1_int = 0;
                if(o1 instanceof StringNode) o1_int = 1;
                else if(o1 instanceof DoubleDotNode) o1_int = 2;
                else if(o1 instanceof SingleSlashNode) o1_int = 3;
                else if(o1 instanceof DoubleSlashNode) o1_int = 4;

                int o2_int = 0;
                if(o2 instanceof StringNode) o2_int = 1;
                else if(o2 instanceof DoubleDotNode) o2_int = 2;
                else if(o2 instanceof SingleSlashNode) o2_int = 3;
                else if(o2 instanceof DoubleSlashNode) o2_int = 4;

                return o1_int - o2_int;
            }
        });
    }

    private String generateString_Call(final TNode node, final String prefix, final String parentMethodName, final Map<TNode, String> node2MethodName) {
        String methodName = null;
        if (node instanceof DoubleDotNode) {
            writer.println(prefix + "{");
            writer.println(prefix + "lexerState = saveState();");
            methodName = parentMethodName + "_DD";
            writer.println(prefix + "if (" + methodName + "(context)){");
            writer.println(prefix + "\treturn true;");
            writer.println(prefix + "}");
            writer.println(prefix + "setState(lexerState);");
            writer.println(prefix + "}");

            node2MethodName.put(node, methodName);
        } else if (node instanceof StringNode) {
            writer.println(prefix + "lexerState = saveState();");
            writer.println(prefix + "do {");

            if (node.getChildren().size() == 0) {
                writer.println(prefix + "\to = next();");
                writer.println(prefix + "\tif (" + generateCondition(true, (StringNode) node) + ") {");
                writer.println(prefix + "\t\t// TODO - implement hit");
                writer.println(prefix + "\t\tTreePathContext.Marker m1 = context.createMarker(\"" + buildMarkerName((StringNode) node) + "\");");
                writer.println(prefix + "\t\tm1.setASTNode((ASTNode)o, " + ((StringNode)node).isDollar() + ");");
                writer.println(prefix + "\t\tcontext.setMetaInfoRef(" + ((StringNode) node).getMetaInfoRef() + ");");
                writer.println(prefix + "\t\treturn true;");
                writer.println(prefix + "\t} else ");
                writer.println(prefix + "\t\tbreak;");
            } else {

                final int[] endType = {-1};
                final String[] methodName1 = {parentMethodName};
                final StringNode[] last = {null};
                iterateOverSequence(node, new SequenceProcessor() {
                    @Override
                    public TNode process(TNode cur) {
                        if (cur instanceof StringNode) {
                            if (cur.getChildren().size() <= 1) {
                                writer.println(prefix + "\to = next();");
                                writer.println(prefix + "\tif (" + generateCondition(false, (StringNode) cur) + ") {");
                                writer.println(prefix + "\t\tbreak;");
                                writer.println(prefix + "\t} else {");
                                writer.println(prefix + "\t\tTreePathContext.Marker m1 = context.createMarker(\"" + buildMarkerName((StringNode) cur) + "\");");
                                writer.println(prefix + "\t\tm1.setASTNode((ASTNode)o, " + ((StringNode)cur).isDollar() + ");");
                                writer.println(prefix + "\t}");

                                methodName1[0] = methodName1[0] + "_" + cur.getName();
                                endType[0] = 1;
                                last[0] = (StringNode) cur;
                                return cur.getChildren().size() == 0 ? null : cur.getChildren().get(0);
                            } else if (cur.getChildren().size() > 1) {
                                writer.println(prefix + "\to = peek();");
                                writer.println(prefix + "\tif (" + generateCondition(true, (StringNode) cur) + ") {");
                                String methodName1 = parentMethodName + "_" + cur.getName();
                                writer.println(prefix + "\t\tif (" + methodName1 + "(context)){");
                                writer.println(prefix + "\t\t\treturn true;");
                                writer.println(prefix + "\t\t}");
                                writer.println(prefix + "\t}");
                                node2MethodName.put(cur, methodName1);
                            }

                        } else {
                            generateString_Call(cur, prefix + "\t", methodName1[0], node2MethodName);
                        }
                        endType[0] = -1;
                        return null;
                    }
                });

                if (endType[0] == 1) {
                    writer.println(prefix + "\t// TODO - implement hit");
                    writer.println(prefix + "\tcontext.setMetaInfoRef(" + last[0].getMetaInfoRef() + ");");
                    writer.println(prefix + "\treturn true;");
                }
            }
            writer.println(prefix + "} while(false);");
            writer.println(prefix + "setState(lexerState);");
            writer.println(prefix + "m.rollbackTo();");

        } else if (node instanceof DoubleSlashNode) {
            writer.println(prefix + "o = peek();");
            writer.println(prefix + "if (o instanceof SlashNode) {");
            writer.println(prefix + "\tlexerState = saveState();");
            methodName = parentMethodName + "_DS";
            writer.println(prefix + "\tif (" + methodName + "(context)){");
            writer.println(prefix + "\t\treturn true;");
            writer.println(prefix + "\t}");
            writer.println(prefix + "\tsetState(lexerState);");
            writer.println(prefix + "}");

            node2MethodName.put(node, methodName);
        } else if (node instanceof SingleSlashNode) {
            writer.println(prefix + "o = peek();");
            writer.println(prefix + "if (o instanceof SlashNode) {");
            writer.println(prefix + "\tlexerState = saveState();");
            methodName = parentMethodName + "_SS";
            writer.println(prefix + "\tif (" + methodName + "(context)){");
            writer.println(prefix + "\t\treturn true;");
            writer.println(prefix + "\t}");
            writer.println(prefix + "\tsetState(lexerState);");
            writer.println(prefix + "}");

            node2MethodName.put(node, methodName);
        }

        return methodName;
    }

    private void iterateOverSequence(TNode node, SequenceProcessor processor) {
        TNode cur = node;
        while (cur != null) {
            cur = processor.process(cur);
        }
    }

    private void generateMethodComment(TNode node) {
        writer.println("/*");
        node.accept(new TNodePrinterVisitor(writer));
        writer.println("\n */");
    }

    private interface SequenceProcessor {
        TNode process(TNode cur);
    }

    private String generateCondition(boolean equals, StringNode node) {
        // n.getElementType() == PlSqlElementTypes." + node.getName() + "
        // n.getElementType() != PlSqlElementTypes." + cur.getName()

        if (node.isDollar()) {
            if (equals) {
                return "o instanceof ASTNode && ((ASTNode)o).getPsi() instanceof " + node.getName();
            } else {
                return "o instanceof ASTNode && !(((ASTNode)o).getPsi() instanceof " + node.getName() + ")";
            }
        } else {
            StringBuilder sb = new StringBuilder();

            if (equals) sb.append("o instanceof ASTNode && ");
            else sb.append("!(o instanceof ASTNode) || ");
            sb.append("((ASTNode)o).getElementType() ");

            if (equals) sb.append(" == ");
            else sb.append(" != ");

            // Check against PlSqlElementTypes
            Class clazz = PlSqlElementTypes.class;
            for (Class ext : clazz.getInterfaces()) {
                for (Field field : ext.getDeclaredFields()) {
                    if (field.getName().equals(node.getName())) {
                        return sb.append("PlSqlElementTypes.").append(node.getName()).toString();
                    }
                }
            }

            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(node.getName())) {
                    return sb.append("PlSqlElementTypes.").append(node.getName()).toString();
                }
            }

            // Check against PlSqlTokenTypes
            Class clazz1 = PlSqlTokenTypes.class;
            for (Class ext : clazz1.getInterfaces()) {
                for (Field field : ext.getDeclaredFields()) {
                    if (field.getName().equals(node.getName())) {
                        return sb.append("PlSqlTokenTypes.").append(node.getName()).toString();
                    }
                }
            }


            for (Class ext : clazz1.getInterfaces()) {
                for (Field field : ext.getDeclaredFields()) {
                    if (field.getName().equals("KEYWORD_" + node.getName())) {
                        return sb.append("PlSqlTokenTypes.").append("KEYWORD_").append(node.getName()).toString();
                    }
                }
            }

            throw new RuntimeException("Cannot find element: " + node.getName());
        }
    }


    private String generateConditionForSS(boolean equals, StringNode node) {
        // n.getElementType() == PlSqlElementTypes." + node.getName() + "
        // n.getElementType() != PlSqlElementTypes." + cur.getName()

        if (node.isDollar()) {
            if (equals) {
                return "n.getPsi() instanceof " + node.getName();
            } else {
                return "!(n.getPsi() instanceof " + node.getName() + ")";
            }
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("n.getElementType() ");
            if (equals) sb.append(" == ");
            else sb.append(" != ");

            // Check against PlSqlElementTypes
            Class clazz = PlSqlElementTypes.class;
            for (Class ext : clazz.getInterfaces()) {
                for (Field field : ext.getDeclaredFields()) {
                    if (field.getName().equals(node.getName())) {
                        return sb.append("PlSqlElementTypes.").append(node.getName()).toString();
                    }
                }
            }

            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(node.getName())) {
                    return sb.append("PlSqlElementTypes.").append(node.getName()).toString();
                }
            }

            // Check against PlSqlTokenTypes
            Class clazz1 = PlSqlTokenTypes.class;
            for (Class ext : clazz1.getInterfaces()) {
                for (Field field : ext.getDeclaredFields()) {
                    if (field.getName().equals(node.getName())) {
                        return sb.append("PlSqlTokenTypes.").append(node.getName()).toString();
                    }
                }
            }


            for (Class ext : clazz1.getInterfaces()) {
                for (Field field : ext.getDeclaredFields()) {
                    if (field.getName().equals("KEYWORD_" + node.getName())) {
                        return sb.append("PlSqlTokenTypes.").append("KEYWORD_").append(node.getName()).toString();
                    }
                }
            }

            throw new RuntimeException("Cannot find element: " + node.getName());
        }
    }


    private String buildMarkerName(StringNode node){
        if (node.isDollar()) {
            return node.getName();
        } else {
            return "#" + node.getName();

        }
    }

}
