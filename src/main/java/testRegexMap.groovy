
fromBankAccount = "55651055649"

println("fromBankAccount is: " + fromBankAccount);

//if (pattern.matcher(fromBankAccount).matches()) {
if (fromBankAccount =~ /^[A-Za-z]{2}/) {
    dbtrAcct_cd = "";
} else {
    dbtrAcct_cd = "BBAN";
}

println("dbtrAcct_cd is: " + dbtrAcct_cd);