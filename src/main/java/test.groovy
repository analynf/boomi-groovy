class Example {
    static void main(String[] args) {
        //println("Hello World!")
        //String contNo = "MSCU9999888";
        String contNo = "1";
        if (contNo?.trim() && contNo.length() > 4) {
            String strCont1 = contNo.substring(0,4);
            String strCont2 = contNo.substring(4, contNo.length());
            println(strCont1 + "-" + strCont2);
        } else {
            println contNo;
        }
        contNo.toUpperCase()
    }
}
