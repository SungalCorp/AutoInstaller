/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import static General.Globals.*;

/**
 *
 * @author danrothman
 */
public class ShelfRefresher implements Runnable {

    public void run() {
        refreshData();
    }
}
