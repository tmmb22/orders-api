\# Orders API



Projeto desenvolvido como prova de conceito para uma aplicação de gestão de encomendas e stock.



---



\## ⚙️ Tecnologias utilizadas

\- \*\*Java 8\*\*

\- \*\*Java EE (Servlets, EJB, JPA/Hibernate)\*\*

\- \*\*PostgreSQL\*\*

\- \*\*WildFly / JBoss\*\*

\- \*\*SLF4J + JUL (logging)\*\*

\- \*\*Git\*\*



---



\## 📦 Funcionalidades principais

\- Criar encomendas de utilizadores.

\- Registar movimentos de stock (entradas e saídas).

\- Atribuir automaticamente stock disponível a encomendas pendentes.

\- Consultar estado de encomendas e saldo livre de stock.



---



\## 🗄️ Estrutura da base de dados

Tabelas principais:

\- \*\*users\*\*: informação dos utilizadores.

\- \*\*items\*\*: catálogo de produtos.

\- \*\*orders\*\*: encomendas feitas pelos utilizadores.

\- \*\*stock\_movements\*\*: entradas/saídas de stock.



---



\## ▶️ Exemplos de utilização (via browser)



1\. \*\*Adicionar stock\*\*

http://localhost:8080/orders-api/test-stock?action=add\&itemId=1\&qty=5



2\. \*\*Criar encomenda\*\*

http://localhost:8080/orders-api/test-order?userId=1\&itemId=1\&quantity=3



3\. \*\*Consultar estado da encomenda\*\*

http://localhost:8080/orders-api/order-status?id=1



4\. \*\*Saldo livre de um item\*\*

http://localhost:8080/orders-api/test-stock?action=freeBalance\&itemId=1





---



\## 👨‍💻 Autor

Tiago Barata



