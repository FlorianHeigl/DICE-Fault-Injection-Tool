package dice.eu.fleximonkey;

import com.jcraft.jsch.*;

import java.io.*;

public class VMmemoryStress {

	public void stressmemory(String host, String vmpassword,String memorytesterloops,String memeorytotal,String sshkeypath) {
		
		OSChecker oscheck = new OSChecker();
		oscheck.oscheck(host, vmpassword, sshkeypath);
		String localOS = oscheck.OSVERSION;
		LoggerWrapper.myLogger.info("Got here");
		LoggerWrapper.myLogger.info(localOS);

		//String localOS = "CENTOS";
		String command;
		
		if (localOS.equals("UBUNTU"))
		{
		command ="dpkg-query -W -f='${Status}' memtester";

		}
		else
		{
			//CENTOS will not accept first command so "dud" command sent
		command ="";
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut(new PrintStream(baos));
		@SuppressWarnings("unused")
		LoggerWrapper loggerWrapper = null;
		try {
			loggerWrapper = LoggerWrapper.getInstance();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		try {
			
			String info = null;

			JSch jsch = new JSch();

			String user = host.substring(0, host.indexOf('@'));
			host = host.substring(host.indexOf('@') + 1);

			Session session = jsch.getSession(user, host, 22);
			  if (sshkeypath.equals("-no")) {
				 session.setPassword(vmpassword);
			  }
			  else if (vmpassword.equals("-no"))
			  {
					 jsch.addIdentity(sshkeypath);
			  }

			session.setPassword(vmpassword);
		
		
		
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
		
			session.connect();
			LoggerWrapper.myLogger.info("Attempting to SSH to VM with ip " + host);
				

			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
					info = new String(tmp, 0, i);
					System.out.print(" Stress Status : " + info);
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: "
							+ channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}

			}
			channel.disconnect();
			String command2 = null;
			if  (localOS.equals("CENTOS"))
			{
				command2 = "sudo wget http://rpms.famillecollet.com/enterprise/remi-release-6.rpm && rpm -Uvh remi-release-6*.rpm && sudp yum -y update && sudo yum install memtester; memtester " + memeorytotal +" "+ memorytesterloops;

				LoggerWrapper.myLogger.info("MemTester tool running");				

				}
			
			else if  (localOS.equals("UBUNTU"))
			
				{
			
				if (info == null) {
					
					command2 = "sudo apt-get -q -y install memtester";
					LoggerWrapper.myLogger.info("MemTester tool not found..Installing......");
				}

				else if (info.equals("install ok installed")) {
					command2 = " sudo memtester "+ memeorytotal +" "+ memorytesterloops;
					LoggerWrapper.myLogger.info("MemTest tool found..running test......");
					LoggerWrapper.myLogger.info("memtester loop number: "
							+ memorytesterloops);
				}
			}

			Channel channel2 = session.openChannel("exec");
			((ChannelExec) channel2).setCommand(command2);
			InputStream in1 = channel2.getInputStream();
			channel2.connect();
			while (true) {
				while (in1.available() > 0) {
					int i = in1.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
					info = new String(tmp, 0, i);
					System.out.print(info);
				}
				if (channel2.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: "
							+ channel2.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}

			}
			in1.close();
			channel2.disconnect();
			session.disconnect();
			LoggerWrapper.myLogger.info( baos.toString());

		} catch (Exception e) {
			LoggerWrapper.myLogger.severe("Unable to SSH to VM " + e.toString());
		}
	}
}
