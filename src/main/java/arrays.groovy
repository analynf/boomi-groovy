secondIncoTerm = "APT Islamabad";

List<String> incoTerms = ["CPT", "CFR", "CIP", "FCA", "FOB"];

println incoTerms.stream().filter { it -> secondIncoTerm.contains(it)}.findFirst().isPresent();
