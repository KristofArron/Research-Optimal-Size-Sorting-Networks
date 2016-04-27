clc
%%
old   = [6 13 21 44 102 159 391 3436 56887 1237241 26637970 489689647]; % ms
old_s = old ./ 1000; % s
old_m = old_s ./ 60; % min
old_u = old_m ./ 60; % uur
old_d = old_u ./ 24; % dag
old_totaal_dag = sum(old_d);

new   = [26 12 20 42 96 410 1147 4875 54304 1079551 22804439 0]; % ms
new_s = new ./ 1000; % s
new_m = new_s ./ 60; % min
new_u = new_m ./ 60; % uur
new_d = new_u ./ 24; % dag
new_totaal_dag = sum(new_d);

%%
figure
plot(2:13, old, '*-')
xlabel('comparator')
ylabel('miliseconden')
set(gca, 'Yscale', 'log')
hold on
plot(2:13, new, 'o-')

%%
figure
plot(2:13, old_u, '*-')
xlabel('comparator')
ylabel('uren')
set(gca, 'Yscale', 'log')
hold on
plot(2:13, new_u, 'o-')

%%
diff = (old_m(1:11) - new_m(1:11));
cumdiff = cumsum(diff);

figure
plot(2:12, diff, '*-')
set(gca, 'Yscale', 'log')
title('Difference')
xlabel('comparator')
ylabel('minuten')
hold on
plot(2:12, cumdiff, 'o')

%%
bar(2:13, [old_m; new_m]')
set(gca, 'Yscale', 'log')