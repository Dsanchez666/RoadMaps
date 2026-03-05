# MySQL Portable

Configured to run MySQL from a USB drive.

# Setup Process
The setup process involves extracting the archive to a USB drive and executing a couple of command scripts.

   #1. Extract archive to a USB drive, keeping the directory structure intact
   2. Run the initialize.cmd script in the \mysql-5.7.30-winx64\scripts directory
   3. Your temporary password is shown the \mysql-5.7.30-winx64\data\%computername%.err file
   4. Start the MySQL portable version, \mysql-5.7.30-winx64\scripts\startup.cmd
   5. Open another command prompt and navigate to \mysql-5.7.30-winx64\bin directory
   6. Login into MySQL using your temporary password via mysql -u root -p
   7. Chnage your root password, 
	mysql> SET PASSWORD FOR 'root'@'localhost' = PASSWORD('new_password');
	mysql> FLUSH PRIVILEGES;
   8. Logout via 
	mysql> quit

