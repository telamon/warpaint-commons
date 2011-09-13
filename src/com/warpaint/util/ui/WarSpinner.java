/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Administrator
 */
public abstract class WarSpinner extends JPanel{
    private JSpinner spinner;
    ProgressLabel pLabel;
    double max,min,step,dvalue;
    
    public WarSpinner(String label,javax.swing.SpinnerModel mod){
        super();
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        spinner=new JSpinner(mod);
        spinner.getModel().addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                Object val = spinner.getValue();
                int ival=0;
                double dval=0.0;
                if(val instanceof Double){
                    dval = ((Double)val).doubleValue();
                    ival = (int)((Double)val).doubleValue();
                }else if(val instanceof Integer){
                    dval = ((Integer)val).intValue()+0.0;
                    ival = ((Integer)val).intValue();
                }
                pLabel.setProgress((float)(dval/(max-min)));
                if(dval!=dvalue){
                    valueChanged(dval,ival);
                }
                dvalue=dval;
            }
        });
        pLabel = new ProgressLabel(label);
        add(pLabel);
        add(spinner);
        setMaximumSize(new Dimension(155,22));
        pLabel.setMaximumSize(new Dimension(85,18));
        spinner.setMaximumSize(new Dimension(55,18));
        setBorder(new javax.swing.border.LineBorder(Color.DARK_GRAY, 1, true));
        MScroll ms = new MScroll();
        addMouseMotionListener(ms);
        addMouseListener(ms);
        pLabel.setProgress((float)(dvalue/(max-min)));
    }
    public JSpinner getSpinner(){
        return spinner;
    }
    public WarSpinner(String label,double start,double min,double max,double step){
        this(label,new javax.swing.SpinnerNumberModel(start,min,max,step));
        this.min=min;
        this.max=max;
        this.step=step;
        this.dvalue=start;
    }
    public WarSpinner(String label,int start,int min,int max,int step){
        this(label,new javax.swing.SpinnerNumberModel(start,min,max,step));
        this.min=min;
        this.max=max;
        this.step=step;
        this.dvalue=start;
    }
    public void setValue(Float var){
        spinner.setValue((double)var);
    }
    public void setValue(Double var){
        spinner.setValue(var);
    }

    public abstract void valueChanged(double dval,int ival);

    class ProgressLabel extends JLabel{
        public ProgressLabel(String t){
            super(t);
        }
        float progress = 0.32f;
        public void setProgress(float p){
            progress=p;
            this.repaint();
        }
        @Override
        public void paint(Graphics g) {
            progress = (float)(dvalue/(max-min));
            g.setColor(UIManager.getColor("Slider.highlight"));//new Color(0x882832));
            g.fillRect(2,0,(int)((getWidth()-3)*progress),getHeight()-1);
            g.setColor(UIManager.getColor("Label.foreground"));
            g.drawString(this.getText(), 3, getHeight()-5);
        }
        
    }

    class MScroll implements MouseMotionListener,MouseListener{
        Point old = null;
        public MScroll(){
            
        }
        public void mouseDragged(MouseEvent e) {
            double v=0.0;
            if(false){ // Relative
            
                if(old==null){
                    return;
                }
                float dampen = 0.1f;
                v = ((e.getPoint().x-old.x)*step)*dampen+((Double)spinner.getValue()).doubleValue();
                spinner.setValue(v>min?(v<max?v:max):min);
                //System.out.println(((e.getPoint().x-old.x)*dragStep)*dampen+"+"+((Double)spinner.getValue()).doubleValue());
                old = e.getPoint();
            }else{ // Absolute
                v = e.getPoint().x*1.0/(pLabel.getWidth() !=0 ?pLabel.getWidth() : 0.5 );
                v=min+(max-min)*v;
            }
            spinner.setValue(v>min?(v<max?v:max):min);
        }

        public void mouseMoved(MouseEvent e) {
            
        }

        public void mouseClicked(MouseEvent e) {

        }

        public void mousePressed(MouseEvent e) {
            old=e.getPoint();
        }

        public void mouseReleased(MouseEvent e) {
            old=null;
        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }

    }
}
