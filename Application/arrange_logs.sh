awk 'BEGIN { outfile = "outside" } { if ($4 == "WEBSITE_VISITED") { print $6 cat >>"websites" } else { print $0 cat >>"log_aggregated" }}' +log.txt
#awk 'BEGIN { outfile = "outside" } { if ($4 == "WEBSITE_VISITED") { print $6; outfile = $6;} else { print $0 cat >"log_"outfile }}' +log.txt
sort -k 3 log_aggregated >> logs_sum.txt
sort -k 3 logs_sum.txt | awk '
BEGIN { start = "DATABASE"; sum = 0}
{
	if ($3 == start) {
		sum += $5 }
	else {
		printf("%d\t", sum);
		sum = $5;
		start = $3;
	}
}

END {printf("%d\n", sumi);}'
