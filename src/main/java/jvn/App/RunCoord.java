package jvn.App;

import jvn.JvnCoord.JvnCoordImpl;

import java.util.Scanner;

public class RunCoord {

    public static void main(String[] args) {
        try {
            JvnCoordImpl coord = new JvnCoordImpl(_Runnable.address, _Runnable.port);
            coord.run();


            System.out.println("Q to quit server");
            Scanner sc = new Scanner(System.in); // not compatible with docker
            String command = sc.nextLine();

            if (command.equals("Q")) {
                coord.stop();
                System.exit(0);
            }

        } catch (Exception e) {
            System.err.println("Error on server :" + e);
            e.printStackTrace();
        }
    }



}
