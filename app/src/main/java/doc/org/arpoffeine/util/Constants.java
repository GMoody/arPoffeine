/*
 * Copyright (C) 2017 Grigory Tureev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package doc.org.arpoffeine.util;

/**
 * Interface for storing general important constants
 */

public interface Constants {

    boolean DEBUG = true;

    String APPLICATION_TAG            = "arPoffeine";
    String ERROR_CHECKING_SU          = "error checking root access";
    String BUNDLE_KEY_SESSION         = "SESSION";
    String CLEANUP_COMMAND_ARPSPOOF   = "killall arpspoof\n";
    String CLEANUP_COMMAND_ARPOFFEINE = "killall arpoffeine\n";

    int NOTIFICATION_ID = 4711;
    int ID_NORMAL 	    = 1;
    int ID_DELETE 	    = 2;
    int ID_SAVE 	    = 3;
    int ID_BLACKLIST    = 4;
    int ID_EXPORT	    = 5;
}
