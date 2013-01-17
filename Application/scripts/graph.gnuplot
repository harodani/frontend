set term postscript
set output "bt.eps"
set yrange [0:100]
set style data histograms
set style histogram rowstacked

set title "Data Transferred"
set ylabel "% of total"
set boxwidth 0.75
set style fill solid border -1
set key invert reverse Left outside

set cbrange [0:3]
unset colorbox
# bright
# set palette defined (0 '#FF0000', 1 '#000000', 2 '#0000FF', 3 '#00FF00')
# data
set palette defined (0 '#579d1c', 1 '#004586', 2 '#333333', 3 '#ff420e')
# data 2
# set palette defined (0 '#ff420e', 1 '#333333', 2 '#004586', 3 '#579d1c')

plot 'data' using (100.*$2/$6):xtic(1) title column(2) lc palette cb 0 linetype 1, \
    for [i=3:5] '' using (100.*column(i)/column(6)) title column(i) lc palette cb i-2 linetype 1

# plot for [i=2:5] 'data' using (100.*column(i)/column(6)) title column(i)

# plot 'data' using (100.*$2/$6):xtic(1) t column(2), \
#     for [i=3:5] '' using (100.*column(i)/column(6)) title column(i)
