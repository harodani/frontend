rm output1
rm log_aggregated
ls phone* -l | awk '{ print $9 }' | xargs cat | awk 'BEGIN { outfile = "outside" } { if ($4 == "WEBSITE_VISITED") { print $6 cat >"websites" } else { print $0 cat >>"log_aggregated" }}'
#awk 'BEGIN { outfile = "outside" } { if ($4 == "WEBSITE_VISITED") { print $6; outfile = $6;} else { print $0 cat >"log_"outfile }}' +log.txt
cat log_aggregated | awk '{ if ($4 == "GET_WITH_FILE") print $0}' > log_getwfile
sort -k 3 log_getwfile > logs_sum.txt
sort -k 3 logs_sum.txt | awk '
BEGIN { start = "DATABASE"; sum = 0}
{
	if ($3 == start) {
		sum += $6 }
	else {
		printf("%d\t", sum);
		sum = $6;
		start = $3;
	}
}

END {printf("%d\n", sum);}' >> output1
