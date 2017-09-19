/* SAAF: A static analyzer for APK files.
 * Copyright (C) 2013  syssec.rub.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.rub.syssec.saaf.gui;

import java.beans.PropertyVetoException;
import java.util.EnumMap;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import de.rub.syssec.saaf.analysis.Analysis;
import de.rub.syssec.saaf.gui.editor.FileTree;
import de.rub.syssec.saaf.gui.frame.CfgSelectorFrame;
import de.rub.syssec.saaf.gui.frame.FoundAPICallsFrame;
import de.rub.syssec.saaf.gui.frame.FoundBytecodeFrame;
import de.rub.syssec.saaf.gui.frame.FoundStringsFrame;
import de.rub.syssec.saaf.model.analysis.AnalysisInterface;
import de.rub.syssec.saaf.model.application.ApplicationInterface;

public class OpenAnalysis {

    private final Analysis analysis;
    private FileTree editorFrame;

    private boolean stringsOpen = false;
    private boolean smaliSearch = false;
    private boolean cfgsOpen = false;
    private boolean callsOpen = false;

    public static enum AppFrame {
        FILETREE,
        SMALI_SEARCH,
        STRINGS,
        CFGS, LOG,
        CALLS
    };

    private final EnumMap<AppFrame, JInternalFrame> frameMap = new EnumMap<AppFrame, JInternalFrame>(AppFrame.class);
    private OpenAppsMgr apps;

    public OpenAnalysis(AnalysisInterface analysis, OpenAppsMgr manager) throws Exception {
        this.analysis = (Analysis) analysis;
        apps = manager;
    }

    /**
     * This method will show a special type of frame for the application managed
     * by this class. If the frame is already available, it will be brought to
     * the front, otherwise a new one is created.
     *
     * @param type the type of the frame
     * @throws Exception
     */
    public void showOrOpenNewFrame(AppFrame type) throws Exception {
        JInternalFrame iFrame = frameMap.get(type);

        if (iFrame == null || iFrame.isClosable()) {
            if (iFrame != null) {
                iFrame.remove(iFrame); // remove old frame
            }			// create a new frame
            switch (type) {
                case FILETREE:
                    iFrame = showFileTree();
                    break;

                case SMALI_SEARCH:
                    iFrame = showSmaliSearch(iFrame);
                    break;

                case STRINGS:
                    iFrame = showStrings(iFrame);
                    break;

                case CFGS:
                    iFrame = showCFG(iFrame);
                    break;

                case CALLS:
                    iFrame = showCalls(iFrame);
                    break;

                default:
                    break;
            }
            // save the window for further access
            frameMap.put(type, iFrame);
            // Display the window.
            //iFrame.setVisible(true);
            //iFrame.toFront();
        } else if (iFrame.isIcon()) {
            try {
                iFrame.setIcon(false);
            } catch (PropertyVetoException e) {
                /* wtf? */
            }
        }
        // now show it, regardless of its old state
        iFrame.setVisible(true);
        iFrame.show();
        iFrame.toFront(); // move to front
    }

    /**
     * @param iFrame
     * @return
     */
    private JInternalFrame showCFG(JInternalFrame iFrame) {
        if (!cfgsOpen) {
            try {
                iFrame = new CfgSelectorFrame(editorFrame.getModel().getCurrentClass());
            } catch (Exception e) {
                e.printStackTrace();
            }
            iFrame.addInternalFrameListener(new InternalFrameListener() {
                public void internalFrameClosing(InternalFrameEvent e) {
                    cfgsOpen = false;
                }

                @Override
                public void internalFrameActivated(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameClosed(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameDeactivated(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameDeiconified(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameIconified(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameOpened(InternalFrameEvent e) {

                }
            });

            MainWindow.getDesktopPane().add(iFrame);
            cfgsOpen = true;
        }
        return iFrame;
    }

    /**
     * @param iFrame
     * @return
     * @throws Exception
     */
    private JInternalFrame showStrings(JInternalFrame iFrame) throws Exception {
        if (!stringsOpen) {
            iFrame = new FoundStringsFrame(analysis.getApp(), editorFrame);

            iFrame.addInternalFrameListener(new InternalFrameListener() {
                public void internalFrameClosing(InternalFrameEvent e) {
                    stringsOpen = false;
                }

                @Override
                public void internalFrameActivated(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameClosed(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameDeactivated(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameDeiconified(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameIconified(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameOpened(InternalFrameEvent e) {

                }
            });

            iFrame.pack();
            MainWindow.getDesktopPane().add(iFrame);
            stringsOpen = true;
        }
        return iFrame;
    }

    /**
     * @param iFrame
     * @return
     */
    private JInternalFrame showSmaliSearch(JInternalFrame iFrame) {
        if (!smaliSearch) {
            iFrame = new FoundBytecodeFrame(analysis.getApp(), editorFrame);

            iFrame.addInternalFrameListener(new InternalFrameListener() {
                public void internalFrameClosing(InternalFrameEvent e) {
                    smaliSearch = false;
                }

                @Override
                public void internalFrameActivated(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameClosed(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameDeactivated(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameDeiconified(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameIconified(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameOpened(InternalFrameEvent e) {

                }
            });

            iFrame.pack();
            MainWindow.getDesktopPane().add(iFrame);
            smaliSearch = true;
        }
        return iFrame;
    }

    /**
     * @param iFrame
     * @return
     */
    private JInternalFrame showCalls(JInternalFrame iFrame) {
        if (!callsOpen) {
            iFrame = new FoundAPICallsFrame(analysis.getApp(), editorFrame);

            iFrame.addInternalFrameListener(new InternalFrameListener() {
                public void internalFrameClosing(InternalFrameEvent e) {
                    callsOpen = false;
                }

                @Override
                public void internalFrameActivated(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameClosed(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameDeactivated(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameDeiconified(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameIconified(InternalFrameEvent e) {

                }

                @Override
                public void internalFrameOpened(InternalFrameEvent e) {

                }
            });

            iFrame.pack();
            MainWindow.getDesktopPane().add(iFrame);
            stringsOpen = true;
        }
        return iFrame;
    }

    /**
     * @return @throws PropertyVetoException
     */
    private JInternalFrame showFileTree() throws PropertyVetoException {
        //JInternalFrame iFrame;
        // Bytecode von apk anzeigen
        //iFrame = new InternalFrameStub("FileTree - Bytecode - "+ analysis.getApp().getApplicationName());

//		iFrame.setBounds(0, 0, iFrame.getWidth(), iFrame.getHeight());
//		iFrame.addInternalFrameListener(new InternalFrameListener(){
//			public void internalFrameClosing(InternalFrameEvent e){
//				apps.closeAnalysis(getAnalysis());
//			}
//
//			@Override
//			public void internalFrameActivated(InternalFrameEvent e) {
//				
//				
//			}
//
//			@Override
//			public void internalFrameClosed(InternalFrameEvent e) {
//				
//				
//			}
//
//			@Override
//			public void internalFrameDeactivated(InternalFrameEvent e) {
//				
//				
//			}
//
//			@Override
//			public void internalFrameDeiconified(InternalFrameEvent e) {
//				
//				
//			}
//
//			@Override
//			public void internalFrameIconified(InternalFrameEvent e) {
//				
//				
//			}
//
//			@Override
//			public void internalFrameOpened(InternalFrameEvent e) {
//				
//				
//			}
//		});
        editorFrame = new FileTree(analysis.getApp(), analysis.getApp().getUnpackedDataDir(), this);
        editorFrame.setBounds(0, 0, editorFrame.getWidth(), editorFrame.getHeight());
        MainWindow.getDesktopPane().add(editorFrame);
        // Add content to the window.
        //iFrame.add(tree);
        editorFrame.pack();
        editorFrame.setMaximum(true);
        return editorFrame;
    }

    public FileTree getTree() {
        return editorFrame;
    }

    /**
     * Close all windows which reference the managed app.
     */
    public void close() {
        for (JInternalFrame iFrame : frameMap.values()) {
            if (iFrame != null) {
                iFrame.dispose();
            }
        }
        frameMap.clear();
    }

    /**
     * @return the associated application
     */
    public ApplicationInterface getApplication() {
        return analysis.getApp();
    }

    public Analysis getAnalysis() {
        return analysis;
    }
}
