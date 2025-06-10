/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.iti.mediationsystem;

import com.iti.engine.MediationEngine;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author theda
 */
public class MediationSystem {

    public static void main(String[] args) {
    MediationEngine engine = new MediationEngine();
    
    while (true) {
        try {
            engine.run();
            Thread.sleep(60000); 
        } catch (InterruptedException ex) {
            Logger.getLogger(MediationSystem.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (Exception ex) {
    Logger.getLogger(MediationSystem.class.getName()).log(Level.SEVERE, "Engine run failed", ex);
        }
    }    }
}
