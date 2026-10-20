CXXFLAGS = -std=c++17 -O3 -DNDEBUG
PUGI = third_party/pugixml

camt_bench: main.o pugixml.o
	$(CXX) $(CXXFLAGS) -o $@ main.o pugixml.o

main.o: main.cpp $(PUGI)/pugixml.hpp
	$(CXX) $(CXXFLAGS) -c main.cpp -o $@

pugixml.o: $(PUGI)/pugixml.cpp $(PUGI)/pugixml.hpp $(PUGI)/pugiconfig.hpp
	$(CXX) $(CXXFLAGS) -c $(PUGI)/pugixml.cpp -o $@

clean:
	rm -f camt_bench main.o pugixml.o

.PHONY: clean
