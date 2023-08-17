stringTest = "";

if (!stringTest?.trim()) {
    println("stringTest");
} else {
    println("else");
}

if (stringTest?.trim() && stringTest.startsWith("SHP-")) {
    println("success");
} else {
    println("fail");
}
