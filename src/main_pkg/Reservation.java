package main_pkg;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import main_pkg.File_IO;
import main_pkg.textDB;


public class Reservation {

	private String count, date, time; //예약 인원수, 날짜, 시간
	private String name; //예약자 이름
	private String phone; //예약자 전화번호
	private String st_num0; //예약된 테이블 번호
	private String st_num1; //예약된 테이블이 붙어있는 경우, 붙어있는 테이블의 번호
	private String[][] menu = new String[4][5]; // menu파일 배열
	private String[] str_menu_num = new String[5];// 사용자가 입력하는 주문수량 string배열
	private int[] int_menu_num = new int[5];// 사용자 입력 주문수량 int배열
	private int stock_result_index = 0;// 재고 부족한 상품저장할 배열의 인덱스
	private int price = 0; // 주문한 총 금액
	private String rsv = "예약";

	File_IO fio = new File_IO();

	/*
	 * print the 'today', user can input date, time, number of people
	 */
	public void set_rsv(String rsv, String name, String phone) {
		this.rsv = rsv;
		this.name = name;
		this.phone = phone;
	}

	public void user_input() {

		LocalDate date_format = LocalDate.now();
		String today = date_format.toString();

		String reserv_date, reserv_time, reserv_count, sub_time;

		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("\n" + rsv + "접수일: " + today);
			System.out.println(rsv + "희망 일자와, 시간, 그리고 방문하는 인원수를 차례대로 입력하세요.");
			System.out.print("→");

			String reservation_input = scanner.nextLine();

			String[] input_value = null;

			// check if format is right
			if (reservation_input.contains("	") || reservation_input.contains(" ")) {
				// for the format of input String
				input_value = reservation_input.trim().split("	");
				if (input_value.length != 3) {
					System.out.println(
							"입력하신 문자열이 올바르지 않습니다. <예약 희망일> + <tab> 1개 + <시간> + <tab> 1개 + <인원수>에 알맞은 형식으로 문자열을 입력해주세요!\n");
					continue;
				}
			} else {
				System.out.println(
						"입력하신 문자열이 올바르지 않습니다. <예약 희망일> + <tab> 1개 + <시간> + <tab> 1개 + <인원수>에 알맞은 형식으로 문자열을 입력해주세요!\n");
				continue;
			}
			if (input_value[0].matches("[0-9]{4,4}" + "-" + "[0-9]{2,2}" + "-" + "[0-9]{2,2}")
					|| input_value[0].matches("[0-9]{8,8}")) {
				// for the format of reservation date
				reserv_date = input_value[0].replaceAll("-", "");
			} else {
				System.out.println("입력하신 문자열이 올바르지 않습니다. <예약 희망일은> yyyy-mm-dd 또는 yyyymmdd의 형식으로 입력해주세요!\n");
				continue;
			}
			if (input_value[1].matches("[0-9][0-9]") || input_value[1].matches("[0-9][0-9]:00")
					|| input_value[1].matches("[0-9][0-9]시")) {
				// for the format of reservation time
				reserv_time = input_value[1];
			} else {
				System.out.println("입력하신 문자열이 올바르지 않습니다. <시간>은 hh 또는 hh:00 또는 hh시의 형식으로 입력해주세요!\n");
				continue;
			}
			if (input_value[2].matches("[0-9]") || input_value[2].matches("[0-9][0-9]")) {
				if (Integer.parseInt(input_value[2]) >= 1 && Integer.parseInt(input_value[2]) <= 12) {
					// for the format of reservation count
					reserv_count = input_value[2];
				} else {
					System.out.println("입력하신 문자열이 올바르지 않습니다. 예약 가능 인원은 최소 1명, 최대 12명입니다!\n");
					continue;
				}
			} else {
				System.out.println("입력하신 문자열이 올바르지 않습니다. <인원수>은 n 또는 nn의 형식으로 입력해주세요!\n");
				continue;
			}

			sub_time = reserv_time.substring(0, 2);

    		if((reserv_date.contentEquals(date_format.plusDays(1).toString()) || reserv_date.replace("-", "").contentEquals(date_format.plusDays(1).toString().replace("-", ""))
    			|| reserv_date.contentEquals(date_format.plusDays(2).toString()) || reserv_date.replace("-", "").contentEquals(date_format.plusDays(2).toString().replace("-", "")))
    			&& (Integer.parseInt(sub_time) >= 10 && Integer.parseInt(sub_time) <= 20)) {
    			//check if value inputed fits data rule
    			this.date = reserv_date;
    			this.time = sub_time;
    			this.count = reserv_count;
    			if(search_table(reserv_date, sub_time, reserv_count).isEmpty() == false) {
    				//check if value inputed is available for reservation
        			break;
    			}else {
    				System.out.println("입력하신 시간대에 해당 인원수가 앉을 수 있는 좌석이 존재하지 않습니다. 다른 시간대를 입력해주시기 바랍니다.\n");
    				continue;
    			}
    		}else {
    			System.out.println("입력하신 문자열이 올바르지 않습니다. 예약은 예약접수일 당일을 제외하고 +2일까지 가능하며, 입력 가능한 시간은 10:00 ~ 20:00입니다.\n");
    			continue;
    		}
    	}
    	show_table(reserv_date, sub_time, reserv_count);

    }

    /*
     * search for available table with inputed day, time, count
     */
    private String search_table(String date, String time, String count){
    	fio.read_file(date.replaceAll("-", ""));
    	String[][][]
    			tmp = fio.tb.get_day();

    	

    	StringBuilder available_tables = new StringBuilder();
    	//should split it with space(" ") to check each available table number
    	//for tables attached, format is "(table number)-(table number)"


    	int tmp_count = 1; //index no. of available number of people to use table
    	int tmp_table = 0; //table number
    	int tmp_time = Integer.parseInt(time) - 10;

    	for(tmp_table = 0; tmp_table < 20; tmp_table++) { //check if there are any table available with chosen day,time,count with availability(3rd value)
    		if(tmp[2][tmp_time][tmp_table].equals("0")) { //if number of people using table is 0
    			if(Integer.parseInt(tmp[tmp_count][tmp_time][tmp_table]) >= Integer.parseInt(count)){
    				//compare available number or people to use table with inputed number of people
    				if(Integer.parseInt(count) < 3 && (tmp[tmp_count][tmp_time][tmp_table]).contentEquals("4") || tmp[tmp_count][tmp_time][tmp_table].equals("6")) {
    					//if current number of people is less than 3, cannot use table for 4 or 6 people
    					continue;
    				}else if(Integer.parseInt(count) < 4 && tmp[tmp_count][tmp_time][tmp_table].contentEquals("6")){
    					//if current number of people is less than 4, cannot use table for 6 people
    					continue;
    				}else { //add string in available_tables
    					available_tables.append(tmp_table + 1);
    					available_tables.append(" ");
    				}
    			}else {
    				continue;
    			}
    		}else {
    			continue;
    		}
    	}
    	int attached_table;
    	if(available_tables.length() == 0) {
    		for(attached_table = 0; attached_table < 20; attached_table++) {
    			//tables can be attached
    			if(attached_table%2 == 0 && tmp[2][tmp_time][attached_table].equals("0") && tmp[2][tmp_time][attached_table++].equals("0")) {
					//if attached tables are both empty
    				if(attached_table >= 0 && attached_table < 6) {
    					//table 1~6
    					if(Integer.parseInt(count) > 2 && Integer.parseInt(count) < 5) {
    						//only more than 2, less than 5 people can use attached tables for 4 people
    						available_tables.append(attached_table);
    						available_tables.append("-");
    						available_tables.append(attached_table + 1);
    						available_tables.append(" ");
    					}else {
    						continue;
    					}
    				}else if (attached_table >= 6 && attached_table < 16) {
    					//table 7~16
    					if(Integer.parseInt(count) > 4 && Integer.parseInt(count) < 9) {
        					//only more than 4, less than 9 people can use attached tables for 8 people
    						available_tables.append(attached_table);
    						available_tables.append("-");
    						available_tables.append(attached_table + 1);
    						available_tables.append(" ");
        				}else {
        					continue;
        				}
    				}else if(attached_table >= 16 && attached_table < 20){
    					if(Integer.parseInt(count) > 8) {
    						//only more than 8 people can use attached tables for 12 people
    						available_tables.append(attached_table);
    						available_tables.append("-");
    						available_tables.append(attached_table + 1);
    						available_tables.append(" ");
    					}else {
    						continue;
    					}
    				}
    			}
    		}
    	}
		return available_tables.toString();
	}

	private void show_table(String date, String time, String count) {
		String[] tmp_date_arr = new String[3];
		String tmp_date = date.replaceAll("-", "");
		// replace all - from date, make it into format of yyyymmdd
		tmp_date_arr[0] = tmp_date.substring(0, 4);
		tmp_date_arr[1] = tmp_date.substring(4, 6);
		tmp_date_arr[2] = tmp_date.substring(6);
		// split inputed reservation date into yyyy mm dd format

		String avail_table = search_table(date, time, count);
		avail_table = avail_table.replaceAll("-", " ");
		// has no. of available tables. same as available_tables in search table
		// function
		String[] every_table = new String[13];
		every_table = avail_table.split(" ");
		// every available tables are in this array

		int[] every_table_int = new int[every_table.length];
		for (int i = 0; i < every_table.length; i++) {
			every_table_int[i] = Integer.parseInt(every_table[i]);
		}
		// change string array into integer array

		Arrays.sort(every_table_int);
		// sort into ascending order

		for (int i = 0; i < every_table_int.length; i++) {
			every_table[i] = Integer.toString(every_table_int[i]);
		}
		// then change integer array into string array again

		String[] table_view = new String[20];
		for (int i = 0; i < 20; i++) {
			table_view[i] = "0";
		}

		// rows of current table chart

		Scanner choose_table_input = new Scanner(System.in);

		while (true) {

			System.out.println("\n" + tmp_date_arr[0] + "년 " + tmp_date_arr[1] + "월 " + tmp_date_arr[2] + "일 " + time
					+ "시 "+rsv+" 가능 좌석 현황");
			System.out.println("--------------------------------------------------");

			// if any available table exists, table view array will be adding . to the table
			// number
			if (every_table != null) {
				for (int i = 0; i < every_table.length; i++) {
					for (int j = 0; j < 20; j++) {
						if (!table_view[j].contains(".")) {
							if (!every_table[i].equals(Integer.toString(j + 1))) {
								table_view[j] = Integer.toString(j + 1);
							} else {
								table_view[j] = Integer.toString(j + 1) + ".";
							}
						}
					}
				}
			}

			// change table view array into 2D array
			String[][] table_show = new String[5][4];
			for (int i = 0; i < 20; i++) {
				if (i < 4) {
					table_show[0][i] = table_view[i];
				} else if (4 <= i && i < 8) {
					table_show[1][i - 4] = table_view[i];
				} else if (8 <= i && i < 12) {
					table_show[2][i - 8] = table_view[i];
				} else if (12 <= i && i < 16) {
					table_show[3][i - 12] = table_view[i];
				} else if (16 <= i) {
					table_show[4][i - 16] = table_view[i];
				}
			}

			// if there is . with table number, it means that table is available
			// except available tables, change it into "//"
			for (int column = 0; column < 5; column++) {
				for (int row = 0; row < 4; row++) {
					if (!table_show[column][row].contains(".")) {
						table_show[column][row] = "//";
						System.out.print("[" + table_show[column][row] + "]\t");
					} else {
						if (Integer.parseInt(table_show[column][row].replace(".", "")) < 10) {
							System.out.print("[0" + table_show[column][row].replace(".", "") + "]\t");
						} else {
							System.out.print("[" + table_show[column][row].replace(".", "") + "]\t");
						}
					}
				}
				System.out.println("");
			}

			System.out.println("--------------------------------------------------\n");

			System.out.println("[01] ~ [06] : 2인용 좌석");
			System.out.println("[07] ~ [16] : 4인용 좌석");
			System.out.println("[17] ~ [20] : 6인용 좌석");
			System.out.println("[//] : "+rsv+" 불가능한 좌석");
			System.out.println("붙어있는 좌석 : (1,2) (3,4) (5,6) (7,8) (9,10) (11,12) (13,14) (15,16) (17,18) (19,20)\n");
			System.out.print("좌석을 자동으로 할당 받으시겠습니까? (y/n) : ");

			String table_choice = choose_table_input.next();

			if (table_choice.trim().equals("y")) {
				auto_table();
				break;
			} else if (table_choice.trim().equals("n")) {
				choose_table();
				break;
			} else {
				System.out.println("입력은 y 혹은 n만 가능합니다. 다시 입력해주세요.");
				continue;
			}
		}

	}

	private void choose_table() {
		String[] tmp_date_arr = new String[3];

		String tmp_date = date.replaceAll("-", "");
		// replace all - from date, make it into format of yyyymmdd
		tmp_date_arr[0] = tmp_date.substring(0, 4);
		tmp_date_arr[1] = tmp_date.substring(4, 6);
		tmp_date_arr[2] = tmp_date.substring(6);
		// split inputed reservation date into yyyy mm dd format

		String avail_table = search_table(date, time, count);
		avail_table = avail_table.replaceAll("-", " ");
		// has no. of available tables. same as available_tables in search table
		// function
		String[] every_table = new String[13];
		every_table = avail_table.split(" ");
		// every available tables are in this array

		int[] every_table_int = new int[every_table.length];
		for (int i = 0; i < every_table.length; i++) {
			every_table_int[i] = Integer.parseInt(every_table[i]);
		}
		// change string array into integer array

		Arrays.sort(every_table_int);
		// sort into ascending order

		for (int i = 0; i < every_table_int.length; i++) {
			every_table[i] = Integer.toString(every_table_int[i]);
		}
		// then change integer array into string array again

		String[] table_view = new String[20];
		for (int i = 0; i < 20; i++) {
			table_view[i] = "0";
		}
		// rows of current table chart

		Scanner table_num = new Scanner(System.in);

		while (true) {

			System.out.println("\n" + tmp_date_arr[0] + "년 " + tmp_date_arr[1] + "월 " + tmp_date_arr[2] + "일 " + time
					+ "시 "+rsv+" 가능 좌석 현황");
			System.out.println("--------------------------------------------------");

			// if any available table exists, table view array will be adding . to the table
			// number
			if (every_table != null) {
				for (int i = 0; i < every_table.length; i++) {
					for (int j = 0; j < 20; j++) {
						if (!table_view[j].contains(".")) {
							if (!every_table[i].equals(Integer.toString(j + 1))) {
								table_view[j] = Integer.toString(j + 1);
							} else {
								table_view[j] = Integer.toString(j + 1) + ".";
							}
						}
					}
				}
			}

			// change table view array into 2D array
			String[][] table_show = new String[5][4];
			for (int i = 0; i < 20; i++) {
				if (i < 4) {
					table_show[0][i] = table_view[i];
				} else if (4 <= i && i < 8) {
					table_show[1][i - 4] = table_view[i];
				} else if (8 <= i && i < 12) {
					table_show[2][i - 8] = table_view[i];
				} else if (12 <= i && i < 16) {
					table_show[3][i - 12] = table_view[i];
				} else if (16 <= i) {
					table_show[4][i - 16] = table_view[i];
				}
			}

			// if there is . with table number, it means that table is available
			// except available tables, change it into "//"
			for (int column = 0; column < 5; column++) {
				for (int row = 0; row < 4; row++) {
					if (!table_show[column][row].contains(".")) {
						table_show[column][row] = "//";
						System.out.print("[" + table_show[column][row] + "]\t");
					} else {
						if (Integer.parseInt(table_show[column][row].replace(".", "")) < 10) {
							System.out.print("[0" + table_show[column][row].replace(".", "") + "]\t");
						} else {
							System.out.print("[" + table_show[column][row].replace(".", "") + "]\t");
						}
					}
				}
				System.out.println("");
			}

			System.out.println("--------------------------------------------------\n");

			System.out.println("[01] ~ [06] : 2인용 좌석");
			System.out.println("[07] ~ [16] : 4인용 좌석");
			System.out.println("[17] ~ [20] : 6인용 좌석");
			System.out.println("[//] : "+rsv+" 불가능한 좌석");
			System.out.println("붙어있는 좌석 : (1,2) (3,4) (5,6) (7,8) (9,10) (11,12) (13,14) (15,16) (17,18) (19,20)\n");

			System.out.print("좌석 번호를 입력하세요: ");
			// 여기까지 거의 복붙

			String inputed_table_num = table_num.nextLine();
			// select table number

			String available = search_table(date, time, count);
			String[] available_table = available.split(" ");

			if (inputed_table_num.contains("\t")) {
				String[] table_num_input = inputed_table_num.trim().split("	");
				if (table_num_input.length == 2) {
					for (int i = 0; i < available_table.length; i++) {
						if (available_table[i].equals(table_num_input[0] + "-" + table_num_input[1])) {
							// attached table is available
							this.st_num0 = table_num_input[0];
							this.st_num1 = table_num_input[1];
							break;
						} else {
							// not matches available attached table
							continue;
						}
					}
					if (st_num0 != null && st_num1 != null) {
						break;
					} else {
						System.out.println("예약이 불가능한 좌석입니다. 다시 입력해주세요.\n");
						continue;
					}
				} else {
					System.out.println("예약이 불가능한 좌석입니다. 다시 입력해주세요.\n");
				}
			} else {
				inputed_table_num = inputed_table_num.trim();
				// table not attached
				for (int i = 0; i < available_table.length; i++) {
					if (inputed_table_num.equals(available_table[i])) {
						// table number matches
						this.st_num0 = inputed_table_num;
						break;
					} else {
						// not matches available table
						continue;
					}
				}
				if (st_num0 != null) {
					break;
				} else {
					System.out.println("예약이 불가능한 좌석입니다. 다시 입력해주세요.\n");
					continue;
				}
			}
		}
		if (rsv.equals("예약 변경")) {
			choose_menu();
		} else {
			input_inform();
		}
	}

	private void auto_table() {

		search_table(date, time, count);

		String available = search_table(date, time, count);
		String[] available_table = available.split(" ");
		if (available_table[0].contains("-")) {
			String[] sp = new String[2];
			sp = available_table[0].split("-");
			this.st_num0 = sp[0];
			this.st_num1 = sp[1];
			System.out.println("할당된 테이블은 [" + st_num0 + "], [" + st_num1 + "]번 입니다.");
			if (rsv.equals("예약 변경")) {
				choose_menu();
			} else {
				input_inform();
			}
		} else {
			this.st_num0 = available_table[0];
			System.out.println("할당된 테이블은 [" + st_num0 + "]번 입니다.");
			if (rsv.equals("예약 변경")) {
				choose_menu();
			} else {
				input_inform();
			}
		}
	}

	private void input_inform() {
		System.out.println("\n[예약정보]");
		if (st_num1 == null) {
			System.out.println("예약 좌석번호 : " + st_num0 + "번");
		} else {
			System.out.println("예약 좌석번호 : " + st_num0 + ", " + st_num1 + "번");
		}

		System.out.println("예약한 인원수 : " + count);
		System.out.println("예약 시간 : " + time + ":00 ~ " + (Integer.parseInt(time) + 2) + ":00");
		System.out.println("-----------------------------------");

		while (true) {
			System.out.println("[필수 입력 정보]");
			System.out.print("이름과 전화번호를 차례대로 입력하세요.(ex.김건국		010-1234-5678 ): ");

			Scanner sc = new Scanner(System.in,"euc-kr");
			String line = sc.nextLine();
			String[] line_split = null;
			line = line.trim();
			// check if format is right
			if (line.contains("	") || line.contains(" ")) {
				// for the format of input String
				line_split = line.split("\t");
				if (line_split.length != 2) {
					System.out.println("입력하신 문자열이 올바르지 않습니다. 이름+<tab> 1개+전화번호의 형식에 맞춰서 정확히 입력하세요!");
					continue;
				}
			} else {
				System.out.println("입력하신 문자열이 올바르지 않습니다. 이름+<tab> 1개+전화번호의 형식에 맞춰서 정확히 입력하세요.");
				continue;
			}

			if (line_split[0].matches("^[가-힣a-zA-Z]*$")) {
				// for the format of reservation date
				this.name = line_split[0];
			} else {
				System.out.println("입력하신 이름이 올바르지 않습니다.\n");
				continue;
			}
			
			if (this.name.length()<2) {
				// for the format of reservation date
				System.out.println("입력하신 이름이 올바르지 않습니다.\n");
				continue;
			} 
			
			
			if (line_split[1].matches("^01(?:0|1)[-](?:\\d{3}|\\d{4})[-]\\d{4}$")
					|| line_split[1].matches("^01(?:0|1)(?:\\d{3}|\\d{4})\\d{4}$")) {
				// for the format of reservation time
				this.phone = line_split[1];
			} else {
				System.out.println("입력하신 전화번호가 올바르지 않습니다.\n");
				continue;
			}
			break;
		}

		if (!this.phone.contains("-")) {
			StringBuilder sb = new StringBuilder();
			sb.append(this.phone);
			sb.insert(3, "-");
			if (this.phone.length() == 10)
				sb.insert(7, "-");
			else
				sb.insert(8, "-");
			this.phone = sb.toString();
		}

		choose_menu();
	}

	private void choose_menu() {

		File_IO file = new File_IO();
		Scanner scan = new Scanner(System.in);
		String patterns0 = "^[가-힣]*";
		String patterns1 = "[0-9]";
		String patterns2 = "[a-zA-Z]";
		String patterns3 = "\t";

		// 메뉴 파일 menu이차원 배열에 저장
		file.read_menu();
		menu = file.tb.get_menu();

		while (true) {
			// 화면 출력
			String temp_num;
			stock_result_index = 0;
			DecimalFormat formatter = new DecimalFormat("###,###");
			price = 0;
			System.out.println("\n--------------------------");
			System.out.println("\t메뉴 선택");
			System.out.println("--------------------------");
			System.out.println("[메뉴]\t[가격]\t\t[주문 가능 시간]");
			for (int i = 0; i < 5; i++) {
				System.out.print(menu[0][i] + "\t￦" + formatter.format(Integer.parseInt(menu[1][i])) + "\t\t");
				if (menu[3][i] != null) {// all이 아닌경우
					String[] menu_time = menu[3][i].split("-");
					System.out.println(menu_time[0] + ":00 ~ " + menu_time[1] + ":00");
				} else {
					System.out.println("all");
				}
			}
			System.out.println();
			for (int i = 0; i < 4; i++) {
				System.out.print(menu[0][i] + ", ");
			}
			System.out.println(menu[0][4] + "의 주문 수량을 차례대로 입력하세요(ex.\t2\t3\t0\t0\t0)");
			System.out.print("→");
			// 주문수량 입력받기
			temp_num = scan.nextLine();
			if (temp_num.contains("\t") || temp_num.contains("")) {
				str_menu_num = temp_num.trim().split("\t");
			}
			// 문법 규칙 위배시
			boolean grammer = true;
			for (int i = 0; i < 5; i++) {
				try {
					if ((Integer.parseInt(str_menu_num[i]) < 0) || (str_menu_num.length != 5)) {
						System.out.println("주문입력 형식에 오류가 있습니다. 입력 방식은 (ex.\t2\t3\t0\t0\t0) 형식입니다 ");
						grammer = false;
						break;
					}
				} catch (NumberFormatException e) {
					System.out.println("주문입력 형식에 오류가 있습니다. 입력 방식은 (ex.\t2\t3\t0\t0\t0) 형식입니다 ");
					grammer = false;
					break;
				}
			}
			if(!grammer) {
				continue;
			}
			
			// 사용자가 입력한 주문 수량 integer 배열에 저장
			for (int i = 0; i < 5; i++) {
				int_menu_num[i] = Integer.parseInt(str_menu_num[i]);
			}

			// 의미 규칙 위배시
			// 1. 주문 불가능한 시간대일 경우
			int time_check_temp = menu_time_check();
			if (time_check_temp != -1) {
				System.out.println(menu[0][time_check_temp] + "을(를) 주문할 수 없습니다. 주문 가능 시간대를 확인해 주세요.");
				continue;
			}
			// 2. 재고가 부족할경우
			int[] stock_temp = menu_stock_check();
			if (stock_result_index != 0) {
				for (int i = 0; i < stock_result_index; i++) {
					System.out.println(menu[0][stock_temp[i]] + "의 수량이 부족합니다(남은 수량:" + menu[2][stock_temp[i]] + "개)");
				}
				continue;
			}

			break;
		}

		menu_confirm();

	}

	// 주문시간대 체크
	private int menu_time_check() {

		for (int i = 0; i < 5; i++) {
			if (int_menu_num[i] != 0) {
				if (menu[3][i] == null) {
					continue;
				} else {
					String[] menu_time = menu[3][i].split("-");
					if ((Integer.parseInt(this.time) < Integer.parseInt(menu_time[0]))
							|| Integer.parseInt(this.time) > (Integer.parseInt(menu_time[1]))) {// 주문가능시간 // 벗어남
						return i;
					}
				}
			}
		}
		return -1;// 주문가능시간!
	}

	// 재고 체크
	private int[] menu_stock_check() {
		int[] result = new int[5];
		for (int i = 0; i < 5; i++) {
			if (int_menu_num[i] != 0) {
				int this_menu_stock = Integer.parseInt(menu[2][i]);
				if (int_menu_num[i] > this_menu_stock) {// 주문수량이 재고보다 많을때
					result[stock_result_index++] = i;
				} // 재고가 부족한 모든 메뉴 저장해서 반환
			}
		}
		return result;
	}

	// 메뉴 확정
	private void menu_confirm() {

		Scanner scan1 = new Scanner(System.in);
		DecimalFormat formatter = new DecimalFormat("###,###");
		while (true) {
			price = 0;
			System.out.println("\n------------------------------");
			System.out.println("\t주문 내역 확인");
			System.out.println("------------------------------");
			System.out.println("[메뉴]\t[가격]\t[주문 수량]");
			for (int i = 0; i < 5; i++) {
				System.out.println(
						menu[0][i] + "\t￦" + formatter.format(Integer.parseInt(menu[1][i])) + "\t" + str_menu_num[i]);
				price += Integer.parseInt(menu[1][i]) * int_menu_num[i];
			}
			System.out.println("\n결제 예정 금액: ￦" + formatter.format(price) + "\n");
			System.out.print("주문 내역을 확정하고 주문을 완료하시겠습니까?(y/n) :");
			String menu_confirm = scan1.nextLine();
			String[] yorn_value = new String[0];

			if (menu_confirm.contains(" ") || menu_confirm.contains("")) {
				yorn_value = menu_confirm.trim().split(" ");
			}

			if (yorn_value[0].equals("y")) {
				String str = "";
				for (int i = 0; i < 5; i++) {
					if (int_menu_num[i] != 0) {
						str += menu[0][i] + " ";
					}
				}
				System.out.println(str + "를(을) 주문합니다.");
				reservation_confirm();
				break;
			} else if (yorn_value[0].equals("n")) {
				choose_menu();
				break;
			} else {
				System.out.println("입력은 y 혹은 n만 가능합니다. 다시 입력해주세요.");
				continue;
			}
		}

	}

	// 예약 확정
	private void reservation_confirm() {
		DecimalFormat formatter = new DecimalFormat("###,###");
		Scanner scan2 = new Scanner(System.in);

		while (true) {

			System.out.println("\n------------------------------\n\t" + rsv + " 내역 확인\n------------------------------");
			System.out.println(rsv + "자 이름: " + this.name);
			System.out.println("전화번호: " + this.phone);
			System.out.println(rsv + " 시간: " + this.time + ":00 ~ " + (Integer.parseInt(this.time) + 2) + ":00");
			System.out.println("인원 수: " + this.count + "명");
			System.out.print(rsv + " 좌석: ");
			if (st_num1 == null) {
				System.out.println(st_num0 + "번");
			} else {
				System.out.println(st_num0 + ", " + st_num1 + "번");
			}
			System.out.print("주문 메뉴: ");
			for (int i = 0; i < 5; i++) {
				if (int_menu_num[i] != 0) {
					System.out.print(menu[0][i] + ": " + int_menu_num[i] + "  ");
				}
			}
			System.out.println();
			System.out.println("결제 예정 금액: ￦" + formatter.format(price) + "\n");
			System.out.print(rsv + "을 확정하시겠습니까?(y/n): ");
			String reservation_confirm = scan2.nextLine();
			String[] yorn_value = new String[0];

			if (reservation_confirm.contains(" ") || reservation_confirm.contains("")) {
				yorn_value = reservation_confirm.trim().split(" ");
			}

			if (yorn_value[0].equals("y")) {
				System.out.println(rsv + "이 완료되었습니다.");
				out_reservation_data();
				break;
			} else if (yorn_value[0].equals("n")) {
				reservation_cancle_confirm();
				break;
			} else {
				System.out.println("입력은 y 혹은 n만 가능합니다. 다시 입력해주세요.");
				continue;
			}
		}

	}

	// 예약 취소
	private void reservation_cancle_confirm() {
		Scanner scan3 = new Scanner(System.in);

		while (true) {
			System.out.println("--------------------------------");
			System.out.println("\t" + rsv + " 취소 확인");
			System.out.println("--------------------------------");
			System.out.println(rsv + " 취소시, 모든 예약 정보가 삭제됩니다.");
			System.out.print("정말 " + rsv + "을 취소하시겠습니까?(y/n): ");

			String reservation_cancel = scan3.next();
			String[] yorn_value = new String[0];
			if (reservation_cancel.contains(" ") || reservation_cancel.contains("")) {
				yorn_value = reservation_cancel.trim().split(" ");
			}

			if (yorn_value[0].equals("y")) {
				System.out.println(rsv + "이 취소되었습니다.");
				break;
			} else if (yorn_value[0].equals("n")) {
				reservation_confirm();
				break;
			} else {
				System.out.println("입력은 y 혹은 n만 가능합니다. 다시 입력해주세요.");
				continue;
			}
		}

	}

	// 예약 확정후 파일에 새로운 정보 저장
	private void out_reservation_data() {
		File_IO file2 = new File_IO();
		file2.read_file(date);

		String[][][] temp = new String[11][12][20];
		temp = file2.tb.get_day();
		int time_fix = Integer.parseInt(time) - 10;
		int st_num0_fix = Integer.parseInt(st_num0) - 1;

		temp[2][time_fix][st_num0_fix] = count;
		temp[2][time_fix + 1][st_num0_fix] = count;

		temp[4][time_fix][st_num0_fix] = name;
		temp[4][time_fix + 1][st_num0_fix] = name;

		temp[5][time_fix][st_num0_fix] = phone;
		temp[5][time_fix + 1][st_num0_fix] = phone;

		temp[6][time_fix][st_num0_fix] = Integer.toString(int_menu_num[0]);
		temp[6][time_fix + 1][st_num0_fix] = Integer.toString(int_menu_num[0]);

		temp[7][time_fix][st_num0_fix] = Integer.toString(int_menu_num[1]);
		temp[7][time_fix + 1][st_num0_fix] = Integer.toString(int_menu_num[1]);

		temp[8][time_fix][st_num0_fix] = Integer.toString(int_menu_num[2]);
		temp[8][time_fix + 1][st_num0_fix] = Integer.toString(int_menu_num[2]);

		temp[9][time_fix][st_num0_fix] = Integer.toString(int_menu_num[3]);
		temp[9][time_fix + 1][st_num0_fix] = Integer.toString(int_menu_num[3]);

		temp[10][time_fix][st_num0_fix] = Integer.toString(int_menu_num[4]);
		temp[10][time_fix + 1][st_num0_fix] = Integer.toString(int_menu_num[4]);

		if (st_num1 != null) {
			int st_num1_fix = Integer.parseInt(st_num1) - 1;

			temp[2][time_fix][st_num1_fix] = count;
			temp[2][time_fix + 1][st_num1_fix] = count;

			temp[4][time_fix][st_num1_fix] = name;
			temp[4][time_fix + 1][st_num1_fix] = name;

			temp[5][time_fix][st_num1_fix] = phone;
			temp[5][time_fix + 1][st_num1_fix] = phone;

			temp[6][time_fix][st_num1_fix] = Integer.toString(int_menu_num[0]);
			temp[6][time_fix + 1][st_num1_fix] = Integer.toString(int_menu_num[0]);

			temp[7][time_fix][st_num1_fix] = Integer.toString(int_menu_num[1]);
			temp[7][time_fix + 1][st_num1_fix] = Integer.toString(int_menu_num[1]);

			temp[8][time_fix][st_num1_fix] = Integer.toString(int_menu_num[2]);
			temp[8][time_fix + 1][st_num1_fix] = Integer.toString(int_menu_num[2]);

			temp[9][time_fix][st_num1_fix] = Integer.toString(int_menu_num[3]);
			temp[9][time_fix + 1][st_num1_fix] = Integer.toString(int_menu_num[3]);

			temp[10][time_fix][st_num1_fix] = Integer.toString(int_menu_num[4]);
			temp[10][time_fix + 1][st_num1_fix] = Integer.toString(int_menu_num[4]);

		}
		file2.tb.set_day(temp);

		// 예약정보 file에 저장
		file2.write_file(this.date);
		// 메뉴파일에서 메뉴이름에 해당하는 메뉴의 메뉴 재고 주문 수량만큼 제외
		file2.read_menu();
		menu = file2.tb.get_menu();
		for (int i = 0; i < 5; i++) {
			if (int_menu_num[i] != 0) {
				int origin_stock = Integer.parseInt(menu[2][i]);
				int new_stock = origin_stock - int_menu_num[i];
				menu[2][i] = Integer.toString(new_stock);
				// 재고 정보 update
				file2.tb.set_menu(menu);
			}
		}
		file2.write_menu();

	}
}
